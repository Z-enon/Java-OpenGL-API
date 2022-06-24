package com.xenon.opengl;

import com.xenon.glfw.OpenGL;

import java.util.*;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL45.*;

public record VertexFormat(VertexFormatElement... elements) {

    private static final Map<Integer, Integer> dataTypeSizes = Map.of(
            GL_BYTE, Byte.BYTES,
            GL_UNSIGNED_BYTE, Byte.BYTES,
            GL_SHORT, Short.BYTES,
            GL_UNSIGNED_SHORT, Short.BYTES,
            GL_INT, Integer.BYTES,
            GL_UNSIGNED_INT, Integer.BYTES,
            GL_FLOAT, Float.BYTES,
            GL_DOUBLE, Double.BYTES
    );
    private static final Map<Integer, String> dataTypes = Map.of(
            GL_INT, "int",
            GL_UNSIGNED_INT, "uint",
            GL_FLOAT, "float",
            GL_DOUBLE, "double"
    );

    /**
     * Ensures that elements are sorted by their location,
     * before calling {@link VertexFormat#VertexFormat(VertexFormatElement...)}.
     * @param elements the elements contained in the format
     * @return a new VertexFormat instance
     */
    public static VertexFormat of(VertexFormatElement... elements) {
        Arrays.sort(elements, Comparator.comparingInt(e -> e.location));
        return new VertexFormat(elements);
    }

    /**
     *
     * @param binding the binding index
     * @return the elements matching the binding index in location growing order
     */
    public Iterable<VertexFormatElement> filter(int binding) {
        return Arrays.stream(elements).filter(f -> f.binding == binding).collect(Collectors.toList());
    }

    /**
     * Computes the stride of the vertex format for the vbo associated to the given binding.
     * (sums the size of all the elements that have a binding point=0)
     *
     * @param binding the buffer binding
     * @return the computed stride
     */
    public int stride(int binding) {
        int i = 0;
        for (var el : elements)
            if (el.binding == binding)
                i += sizeof(el.type) * el.count;
        return i;
    }

    /**
     * Implementation-wise equivalent to:
     * <code><pre>
     *     someFormat.elements().foreach( e -> {
     *         glEnableVertexAttribArray( e.location() );
     *         glVertexAttrib*Format( e.location(), e.count(), e.type(), e.normalized(), offset(vboStruct, e) );
     *         glVertexAttribBinding( e.location(), e.binding() );
     *         glVertexBindingDivisor( e.binding(), e.divisor() );
     *     });
     * </pre></code>
     * with <code>e.normalized()</code> only being used with glVertexAttribFormat()
     * (not with glVertexAttribLFormat nor glVertexAttribIFormat).
     * @param vao the VAO id
     */
    @OpenGL("Requires DSA support (OpenGL 4.5+)")
    public void attribSetup(final int vao) {
        Map<Integer, Integer> offsetByBuffer = new HashMap<>();
        for (VertexFormatElement el : elements) {
            int loc = el.location;
            int bind = el.binding;
            int co = el.count;
            int ty = el.type;
            boolean norm = el.normalized;
            int size = sizeof(ty) * co;
            int off;
            Integer cached_offset = offsetByBuffer.get(bind);
            off = Objects.requireNonNullElse(cached_offset, 0);

            glEnableVertexArrayAttrib(vao, loc);
            if (norm || ty == GL_FLOAT)   // 32-bit floating point
                glVertexArrayAttribFormat(vao, loc, co, ty, norm, off);
            else if (ty == GL_DOUBLE)    // 64-bit floating point
                glVertexArrayAttribLFormat(vao, loc, co, ty, off);
            else glVertexArrayAttribIFormat(vao, loc, co, ty, off); // integer type
            glVertexArrayAttribBinding(vao, loc, bind);

            offsetByBuffer.put(bind, off + size);
        }
    }

    public static int sizeof(int type) {
        return dataTypeSizes.get(type);
    }

    public static class VertexFormatElement extends DataFormatElement {

        private static String computeGLSLType(int count, int type, boolean normalized) {
            if (normalized)
                return count == 1 ? "float" : "vec" + count;

            String glsl_type = dataTypes.get(type);
            if (count == 1)
                return glsl_type;
            char c = glsl_type.charAt(0);
            String s = "vec";
            if (c != 'f')
                s = c + s;
            return s + count;
        }

        public final int binding, count, type;
        public final boolean normalized;

        public VertexFormatElement(int location, int binding, int count, int type, boolean normalized) {
            this(location, binding, count, type, normalized, null);
        }
        public VertexFormatElement(int location, int binding, int count, int type, boolean normalized, String name) {
            super(location, computeGLSLType(count, type, normalized), new String[] {"in"}, name);
            this.binding = binding;
            this.count = count;
            this.type = type;
            this.normalized = normalized;
        }

    }

}

package com.xenon.opengl.abstraction;


import com.xenon.glfw.ModelUtils;
import com.xenon.glfw.OpenGL;
import com.xenon.glfw.ShaderProgram;
import com.xenon.opengl.VertexFormat;
import com.xenon.utils.MathsTools;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import static com.xenon.glfw.GLTools.*;
import static org.lwjgl.opengl.GL45.*;

/**
 * @author Zenon
 */
@OpenGL("Requires OpenGL 4.5 (DSA + MDI + Persistent Mapping)")
public abstract class AbstractQuadRenderer implements WorldRenderer {

    /**
     * Effectively
     * <code>((MathsTools.roundPowerOfTwo(init_quadCapacity * 4 * stride) / stride) / 4)</code>
     * @param init_quadCapacity the initial quadCapacity
     * @param stride the stride of the vbo for each vertex
     * @return the closest quadCapacity to the initial quadCapacity rounded to a power of two
     */
    public static int closestQuadCapacityToPowerOfTwo(int init_quadCapacity, int stride) {
        return (MathsTools.roundPowerOfTwo(init_quadCapacity * 4 * stride) / stride) >> 2;
    }

    protected static final int VBO_BDG = 0;

    protected ShaderProgram shaderProgram;
    protected final ByteBuffer vbo_data;

    protected final int vao, ebo, vbo, ibo;
    protected final int quad_capacity;
    protected final VertexFormat format;

    protected int vertexCount;

    /**
     * Ensures that quadCapacity is the closest possible to a power of two before calling
     * {@link #AbstractQuadRenderer(int, VertexFormat, Void)}.
     * @param quadCapacity the quad capacity
     * @param format the VertexFormat
     * @see #AbstractQuadRenderer(int, VertexFormat, Void)
     */
    protected AbstractQuadRenderer(int quadCapacity, VertexFormat format) {
        this(closestQuadCapacityToPowerOfTwo(quadCapacity, format.stride(0)), format, null);
    }

    /**
     * Create the quad renderer, assuming <code>quadCapacity</code> is the closest possible to a power of two.
     * @param quadCapacity the closest quad capacity possible to a given power of two
     * @param format the VertexFormat
     * @param sig here to differentiate 2 constructors
     * @see #AbstractQuadRenderer(int, VertexFormat)
     */
    @SuppressWarnings("unused")
    protected AbstractQuadRenderer(int quadCapacity, VertexFormat format, Void sig) {
        this.format = format;
        quad_capacity = quadCapacity;
        int vbo_stride = format.stride(VBO_BDG);
        vao = glCreateVertexArrays();
        ebo = glCreateBuffers();
        vbo = glCreateBuffers();
        ibo = glCreateBuffers();
        glVertexArrayVertexBuffer(vao, VBO_BDG, vbo, 0, vbo_stride);
        glVertexArrayElementBuffer(vao, ebo);
        format.attribSetup(vao);


        long vbo_size = MathsTools.roundPowerOfTwo(quad_capacity * 4 * vbo_stride);
        int flags = GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT;
        glNamedBufferStorage(vbo, vbo_size, flags);
        vbo_data = Objects.requireNonNull(glMapNamedBufferRange(vbo, 0, vbo_size,
                flags | GL_MAP_FLUSH_EXPLICIT_BIT));

        try (MemoryStack stack = MemoryStack.stackPush()) {
            glNamedBufferStorage(ebo, wrap(ModelUtils.genQuadIndicesS(quad_capacity), stack), 0);

            IntBuffer ibo_data = stack.mallocInt(quad_capacity * 5);
            int baseVertex = 0;
            for (int i = 0; i < quad_capacity; i++) {
                ibo_data.put(6).put(1).put(0).put(baseVertex).put(i);
                baseVertex += 4;
            }
            bindVAO(vao);
            bindIBO(ibo);
            glBufferStorage(GL_DRAW_INDIRECT_BUFFER, ibo_data.flip(), 0);
        }
    }

    @Override
    public void build(ShaderProgram attachedProgram) {
        shaderProgram = attachedProgram;
    }

    @Override
    public VertexFormat format() {
        return format;
    }

    @Override
    public WorldRenderer pos(double x, double y) {
        vbo_data.putFloat((float) x).putFloat((float) y);
        return this;
    }

    @Override
    public WorldRenderer pos(double x, double y, double z) {
        vbo_data.putFloat((float) x).putFloat((float) y).putFloat((float) z);
        return this;
    }

    @Override
    public WorldRenderer tex(double u, double v) {
        vbo_data.putFloat((float) u).putFloat((float) v);
        return this;
    }

    @Override
    public WorldRenderer color(int r, int g, int b, int a) {
        vbo_data.put((byte) r).put((byte) g).put((byte) b).put((byte) a);
        return this;
    }

    @Override
    public void endVertex() {
        vertexCount++;
    }

    @Override
    public void GPU() {
        if (vertexCount == 0)   return;
        preGPU();
        glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_SHORT, 0, vertexCount >> 2, 0);
        postGPU();
    }

    /**
     * Ensures OpenGL get the correct data. binds everything
     */
    protected void preGPU() {
        glFlushMappedNamedBufferRange(vbo, 0, vbo_data.position());
        shaderProgram.bind();
        bindVAO(vao);
        bindIBO(ibo);
    }

    /**
     * Clear buffers and reset vertex count.
     */
    protected void postGPU() {
        vbo_data.clear();
        vertexCount = 0;
    }

    @Override
    public void dispose() {
        shaderProgram.dispose();
        unbindVAO();
        glDeleteVertexArrays(vao);
        unbindVBO();
        glDeleteBuffers(vbo);
        unbindIBO();
        glDeleteBuffers(ibo);
        unbindEBO();
        glDeleteBuffers(ebo);
    }
}

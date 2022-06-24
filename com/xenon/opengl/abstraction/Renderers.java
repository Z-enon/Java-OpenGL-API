package com.xenon.opengl.abstraction;

import com.xenon.glfw.GLTools;
import com.xenon.glfw.ShaderProgram;
import com.xenon.opengl.VertexFormat;
import com.xenon.opengl.debug.Circe;
import com.xenon.opengl.debug.Polypheme;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static org.lwjgl.opengl.GL46.*;
/**
 * Blending is supported but is extremely expensive on batching.
 * See {@link InstancedQuadRenderer} and {@link GLTools#enableBlend()} to see how we handle it.
 *
 * @author Zenon
 */
public class Renderers {

    public static WorldRenderer POS2_COL;
    public static WorldRenderer POS2_TEX;
    public static WorldRenderer POS2_TEX_COL;

    private static WorldRenderer[] renderers;

    /**
     * Creates the default renderers as well as their shaders.
     * @param width the initial width of the application
     * @param height the initial height of the application
     * @param colQuadCap {@link #POS2_COL} capacity
     * @param texQuadCap {@link #POS2_TEX} capacity
     * @param texColQuadCap {@link #POS2_TEX_COL} capacity
     */
    public static void init(int width, int height, int colQuadCap, int texQuadCap, int texColQuadCap) {

        Polypheme.registerLib("XENON_UI_ESSENTIALS", String.format("""
                vec2 correct2D(vec2 u)
                {
                    return vec2( (2.0 / %d) * u.x - 1.0, 1.0 - u.y * (2.0 / %d) );
                }
                """, width, height));

        POS2_COL = new POS2_COL_Renderer(colQuadCap);
        POS2_TEX = new POS2_TEX_Renderer(texQuadCap);
        POS2_TEX_COL = new POS2D_TEX_COL_Renderer(texColQuadCap);
        registerRenderer(POS2_COL, POS2_TEX, POS2_TEX_COL);

        try {
            POS2_COL.build(ShaderProgram.build(Circe.parseVertexAndFragment(
                    Files.readString(Paths.get("./assets/shaders/ui_col.glsl")),
                    POS2_COL.format()
            )));
            POS2_TEX.build(ShaderProgram.build(Circe.parseVertexAndFragment(
                    Files.readString(Paths.get("./assets/shaders/ui_tex.glsl")),
                    POS2_TEX.format()
            )));
            POS2_TEX_COL.build(ShaderProgram.build(Circe.parseVertexAndFragment(
                    Files.readString(Paths.get("./assets/shaders/ui_tex_col.glsl")),
                    POS2_TEX_COL.format()
            )));
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * registers the given WorldRenderers
     * @param rs the WorldRenderers
     */
    public static void registerRenderer(WorldRenderer... rs) {
        renderers = merge(renderers, rs);
    }

    /*
    * Merges a2 with a1 and returns the final array.
    * */
    private static WorldRenderer[] merge(WorldRenderer[] a1, WorldRenderer[] a2) {
        assert a1 != null || a2 != null;
        if (a1 == null || a1.length == 0)
            return a2;
        if (a2 == null || a2.length == 0)
            return a1;
        int len1 = a1.length, len2 = a2.length;
        WorldRenderer[] r = new WorldRenderer[len1 + len2];

        System.arraycopy(a1, 0, r, 0, len1);
        System.arraycopy(a2, 0, r, len1, len2);

        return r;
    }

    /**
     * Draws the content of all the registered WorldRenderers
     */
    public static void draw() {
        for (var w : renderers)
            w.GPU();
    }

    /**
     * Disposes of all the registered WorldRenderers
     */
    public static void dispose() {
        for (var w : renderers)
            w.dispose();
    }


    /**
     * Binds the texture for future rendering operations.
     * @param id the texture id
     */
    public static void bindTexture(int id) {
        Textured2DQuadRenderer.currentTextureID = id;
    }

    public static void zlevel(int lvl) {
        Depth2DRenderer.zlevel = lvl;
    }

    private static UnsupportedOperationException uoe() {
        return new UnsupportedOperationException();
    }


    // WorldRenderer abstractions

    public abstract static class InstancedQuadRenderer extends AbstractQuadRenderer {

        protected static final int INST_VBO_BDG = 1;

        protected final ByteBuffer instanced_vbo_data;
        protected final int instanced_vbo;

        protected InstancedQuadRenderer(int quadCapacity, VertexFormat format) {
            super(quadCapacity, format);

            int i_stride = format.stride(INST_VBO_BDG);
            long i_size = (long) this.quad_capacity * i_stride;
            instanced_vbo = glCreateBuffers();

            glVertexArrayVertexBuffer(vao, INST_VBO_BDG, instanced_vbo, 0, i_stride);
            glVertexArrayBindingDivisor(vao, INST_VBO_BDG, 1);

            int flags = GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT;
            glNamedBufferStorage(instanced_vbo, i_size, flags);
            instanced_vbo_data = Objects.requireNonNull(glMapNamedBufferRange(instanced_vbo, 0, i_size,
                    flags | GL_MAP_FLUSH_EXPLICIT_BIT));

        }

        @Override
        public void endVertex() {
            super.endVertex();
            if ((vertexCount & 3) == 0) {
                on4thVertex();
                if (GLTools.blend)  // if 2D blend is enabled, flush instantly for correctness
                    GPU();
            }
        }
        protected abstract void on4thVertex();

        @Override
        protected void preGPU() {
            glFlushMappedNamedBufferRange(instanced_vbo, 0, instanced_vbo_data.position());
            super.preGPU();
        }

        @Override
        protected void postGPU() {
            super.postGPU();
            instanced_vbo_data.clear();
        }
    }

    public abstract static class Depth2DRenderer extends InstancedQuadRenderer {

        static int zlevel;

        protected Depth2DRenderer(int quadCapacity, VertexFormat format) {
            super(quadCapacity, format);
        }

        @Override
        protected void on4thVertex() {
            instanced_vbo_data.put((byte) zlevel);
        }

        /**
         * @throws UnsupportedOperationException always
         */
        @Override
        public WorldRenderer pos(double x, double y, double z) {
            throw uoe();
        }
    }

    public abstract static class Textured2DQuadRenderer extends Depth2DRenderer {

        static long currentTextureID;

        protected Textured2DQuadRenderer(int quadCapacity, VertexFormat format) {
            super(quadCapacity, format);
        }

        @Override
        protected void on4thVertex() {
            if (currentTextureID == 0)
                throw new RuntimeException("No texture bound");
            instanced_vbo_data.putLong(currentTextureID);
            super.on4thVertex();
        }
    }


    // WorldRenderer implementations

    public static class POS2_COL_Renderer extends Depth2DRenderer {
        /**
         * respectively pos, color & zlevel
         */
        private static final VertexFormat stc_format = VertexFormat.of(
                new VertexFormat.VertexFormatElement(0, VBO_BDG, 2, GL_FLOAT, false),
                new VertexFormat.VertexFormatElement(1, VBO_BDG, 4, GL_UNSIGNED_BYTE, true),
                new VertexFormat.VertexFormatElement(2, INST_VBO_BDG, 1, GL_UNSIGNED_BYTE, true)
        );

        public POS2_COL_Renderer(int quadCapacity) {
            super(quadCapacity, stc_format);
        }

        /**
         * @throws UnsupportedOperationException always
         */
        @Override
        public WorldRenderer tex(double u, double v) {
            throw uoe();
        }
    }

    public static class POS2_TEX_Renderer extends Textured2DQuadRenderer {
        /**
         * respectively pos, tex, texID & zlevel
         */
        private static final VertexFormat stc_format = VertexFormat.of(
                new VertexFormat.VertexFormatElement(0, VBO_BDG, 2, GL_FLOAT, false),
                new VertexFormat.VertexFormatElement(1, VBO_BDG, 2, GL_FLOAT, false),
                new VertexFormat.VertexFormatElement(2, INST_VBO_BDG, 2, GL_UNSIGNED_INT, false),
                new VertexFormat.VertexFormatElement(3, INST_VBO_BDG, 1, GL_UNSIGNED_BYTE, true)
        );

        public POS2_TEX_Renderer(int quadCapacity) {
            super(quadCapacity, stc_format);
        }

        /**
         * @throws UnsupportedOperationException always
         */
        @Override
        public WorldRenderer color(int r, int g, int b, int a) {
            throw new UnsupportedOperationException("color function used in non-colored context");
        }
    }

    public static class POS2D_TEX_COL_Renderer extends Textured2DQuadRenderer {
        /**
         * respectively pos, tex, color, texID & zlevel
         */
        private static final VertexFormat stc_format = VertexFormat.of(
                new VertexFormat.VertexFormatElement(0, VBO_BDG, 2, GL_FLOAT, false),
                new VertexFormat.VertexFormatElement(1, VBO_BDG, 2, GL_FLOAT, false),
                new VertexFormat.VertexFormatElement(2, VBO_BDG, 4, GL_UNSIGNED_BYTE, true),
                new VertexFormat.VertexFormatElement(3, INST_VBO_BDG, 2, GL_UNSIGNED_INT, false),
                new VertexFormat.VertexFormatElement(4, INST_VBO_BDG, 1, GL_UNSIGNED_BYTE, true)
        );

        public POS2D_TEX_COL_Renderer(int quadCapacity) {
            super(quadCapacity, stc_format);
        }

    }

}

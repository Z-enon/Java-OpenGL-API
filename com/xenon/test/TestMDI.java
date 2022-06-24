package com.xenon.test;

import com.xenon.glfw.GLFWContext;
import com.xenon.opengl.VertexFormat;
import com.xenon.opengl.debug.Circe;
import com.xenon.opengl.debug.DebugContext;
import com.xenon.opengl.debug.TestUnit;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.Random;

import static com.xenon.glfw.GLTools.bindIBO;
import static com.xenon.glfw.GLTools.bindVAO;
import static org.lwjgl.opengl.GL45.*;

/**
 * @author Zenon
 */
public class TestMDI extends TestUnit {

    public static void main(String[] a) {
        new TestMDI().run();
    }

    static final VertexFormat format = VertexFormat.of(
            new VertexFormat.VertexFormatElement(0, 0, 2, flt, false, ""),
            new VertexFormat.VertexFormatElement(1, 0, 4, uch, true, ""),
            new VertexFormat.VertexFormatElement(2, 1, 1, GL_UNSIGNED_INT, false),
            new VertexFormat.VertexFormatElement(3, 1, 1, GL_UNSIGNED_BYTE, true, "")
    );

    TestMDI() {
        super(800, 500, GLFWContext.build(4, 6, true),
                Circe.parseVertexAndFragment(sha, format));
    }

    static String sha = """
            #vertex
            #inputs <0: vec2/pos; 1: vec4/col; 2: uint/zlevel; 3:float/alpha>
            #outputs <0: vec4/outCol>
            
            void main()
            {
                float z = float(zlevel) / 255.0;
                outCol = vec4(col.xyz, col.w * alpha);
                gl_Position = vec4(pos, z, 1.0);
            }
            
            #fragment
            #inputs <0: vec4/inCol>
            #outputs <0: vec4/outCol>
            
            void main()
            {
                outCol = inCol;
            }
            
            """;

    int vao, vbo, ivbo, ebo, ibo;
    final int quadCount = 3;

    @Override
    public void init() {
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glClearColor(1, 1, 1, 1);
        DebugContext.createContext();
        vao = glCreateVertexArrays();
        vbo = glCreateBuffers();
        ivbo = glCreateBuffers();
        ebo = glCreateBuffers();
        ibo = glCreateBuffers();
        format.attribSetup(vao);
        glVertexArrayElementBuffer(vao, ebo);
        glVertexArrayVertexBuffer(vao, 0, vbo, 0, format.stride(0));
        glVertexArrayVertexBuffer(vao, 1, ivbo, 0, format.stride(1));
        glVertexArrayBindingDivisor(vao, 1, 1);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer bb2 = stack.malloc((2 * s_flt + 4) * 4 * quadCount);
            quickSample(bb2, quadCount);
            glNamedBufferData(vbo, bb2, stc);
            glNamedBufferData(ebo, quickIndices(stack, quadCount), stc);

            ByteBuffer bb1 = stack.malloc( (1 + Integer.BYTES) * quadCount);

            ByteBuffer bb = stack.malloc(s_in * 5 * quadCount);
            bb1.putInt(249).put((byte) 255).putInt(250).put((byte) 255).putInt(254).put((byte) 255);
            Random random = new Random();

            for (int i = 0; i < quadCount; i++) {
                //bb1.put((byte) random.nextInt(256));
                bb.putInt(6).putInt(1).putInt(0).putInt(i * 4).putInt(i);
            }
            bindVAO(vao);
            bindIBO(ibo);
            glBufferData(ind_buf, bb.flip(), stc);
            glNamedBufferData(ivbo, bb1.flip(), stc);
        }

    }


    @Override
    protected void draw() {
        bindVAO(vao);
        bindIBO(ibo);
        glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_SHORT, 0, quadCount, 0);
    }
}

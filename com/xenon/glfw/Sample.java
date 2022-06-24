package com.xenon.glfw;

import com.xenon.glfw.abstraction.App;
import com.xenon.opengl.debug.Circe;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

import static com.xenon.glfw.GLTools.*;
import static org.lwjgl.opengl.GL40.*;

/**
 * Renders a few rainbow blocks.
 * Every opengl function starts with "gl", so it prevents confusion with GLTools static imports.
 * For an in-depth opengl guide, you should visit <a href="https://learnopengl.com/">Learn OpenGL</a>.
 * @author Zenon
 */
public class Sample implements App {


    /* used for drawing instanced meshes */
    final int instanceCount = 10;

    /* the java projection matrix object */
    Matrix4f projectionMatrix = new Matrix4f();


    final float fov = (float) Math.toRadians(100d);

    /* tell whether the dimension and the projection matrix in the ubo need update */
    boolean uboDimensionNeedsUpdate;

    /* the window object */
    Window window;

    /* the shader program, as we are only using one here*/
    ShaderProgram shaderProgram;

    /* Our cube's mesh */
    Mesh mesh;

    // OpenGL Objects' handles
    int vao, vbo, instancedVbo, ebo, ubo;
    /*
    * little memo
    * vao:  vertex array object. Container for other buffers.
    *       Typically, you use a VAO for font rendering and several ones for object rendering.
    *
    * vbo:  vertex buffer object. Allow passing vertex data to the GPU.
    *
    * ebo:  element buffer object. Allow indexing as OpenGL 3+ no longer supports GL_QUADS,
    *       in order to draw 2 triangles with only 4 vertices.
    *
    * ubo:  uniform buffer object. Allow passing data to uniform blocks in shader programs.
    *       Prefer this over lots of uniform variables.
    * */

    /**
     * Notice that we can barely reduce the number of lines by using Objects over handles for buffers & vao.
     * Well, maybe except for the VertexAttribPointer declaration, but that's pretty much everything.
     * That's just how LWJGL3 is designed, and why nobody wants to port LWJGL2 code to 3.
     */
    @Override
    public void init(){

        final int meshCapacity = 384;
        mesh = new Mesh(meshCapacity);
        mesh.putF(0, 0, 1).putUnsignedByte(255, 0, 255, 255);   // x y z r g b a pattern
        mesh.putF(1, 0, 1).putUnsignedByte(0, 255, 255, 255);
        mesh.putF(1, 1, 1).putUnsignedByte(255, 255, 0, 255);
        mesh.putF(0, 1, 1).putUnsignedByte(0, 255, 0, 255);

        mesh.putF(0, 0, 0).putUnsignedByte(255, 0, 255, 255);
        mesh.putF(0, 1, 0).putUnsignedByte(0, 255, 255, 255);
        mesh.putF(1, 1, 0).putUnsignedByte(255, 255, 0, 255);
        mesh.putF(1, 0, 0).putUnsignedByte(0, 255, 0, 255);

        mesh.putF(0, 1, 0).putUnsignedByte(255, 0, 255, 255);
        mesh.putF(0, 1, 1).putUnsignedByte(0, 255, 255, 255);
        mesh.putF(1, 1, 1).putUnsignedByte(255, 255, 0, 255);
        mesh.putF(1, 1, 0).putUnsignedByte(0, 255, 0, 255);

        mesh.putF(0, 0, 0).putUnsignedByte(255, 0, 255, 255);
        mesh.putF(1, 0, 0).putUnsignedByte(0, 255, 255, 255);
        mesh.putF(1, 0, 1).putUnsignedByte(255, 255, 0, 255);
        mesh.putF(0, 0, 1).putUnsignedByte(0, 255, 0, 255);

        mesh.putF(1, 1, 1).putUnsignedByte(255, 0, 255, 255);
        mesh.putF(1, 0, 1).putUnsignedByte(0, 255, 255, 255);
        mesh.putF(1, 0, 0).putUnsignedByte(255, 255, 0, 255);
        mesh.putF(1, 1, 0).putUnsignedByte(0, 255, 0, 255);

        mesh.putF(0, 0, 0).putUnsignedByte(255, 0, 255, 255);
        mesh.putF(0, 0, 1).putUnsignedByte(0, 255, 255, 255);
        mesh.putF(0, 1, 1).putUnsignedByte(255, 255, 0, 255);
        mesh.putF(0, 1, 0).putUnsignedByte(0, 255, 0, 255); // yeah, that's a cube


        final int width = 800, height = 500;

        GLFWContext.build(4, 2, true);

        window = Window.build("Sample", width, height, new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long handle, int width, int height) {
                glViewport(0, 0, width, height);     // update the view port.
                // without it, the vertex data statically drawn will not be updated!
                window.width = width;
                window.height = height;
                uboDimensionNeedsUpdate = true; // marking the UBO dirty for next render loop
            }
        });
        window.center();

        try{
            shaderProgram = ShaderProgram.build(
                    Files.readString(Paths.get("./src/com/xenon/glfw/shaders/sample.vs")),
                    Files.readString(Paths.get("./src/com/xenon/glfw/shaders/sample.fs"))
            );
        }catch(IOException e){
            throw new IllegalStateException(e);
        }
        glEnable(GL_DEPTH_TEST);    // glEnable must be used after a window has been initialized.
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);  // basic blending function. necessary for decent rendering

        // init VAO
        vao = glGenVertexArrays();
        glBindVertexArray(vao); // bind it


        // init ebo and fill it statically, as indices will never change
        ebo = glGenBuffers();
        bindEBO(ebo);   // bind it
        try(MemoryStack stack = MemoryStack.stackPush()){
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, wrap(ModelUtils.genQuadIndicesS(6), stack), GL_STATIC_DRAW);
        }
        // here, we don't unbind the ebo because it's far from useful. Indeed, it'll stay bind with the current VAO.
        // That doesn't you can't switch EBO with another VAO. Usually, we have 1 EBO for a VAO.


        // init vbo and allocate storage for dynamic draw. The vertices won't change from one loop call to another,
        // we just use GL_DYNAMIC_DRAW to demonstrate how it works.
        vbo = glGenBuffers();
        bindVBO(vbo);

        // allocate sizeof(mesh) storage for this vbo
        glBufferData(GL_ARRAY_BUFFER, meshCapacity, GL_DYNAMIC_DRAW);

        // stride of the data for one vertex
        int stride = 3 * Float.BYTES + 4;

        // glVertexAttribPointer maps pointers information with the currently bound VBO, so be sure yours is bound.
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
        glVertexAttribPointer(1, 4, GL_UNSIGNED_BYTE, true, stride, 3 * Float.BYTES);
        // ^ here, normalized parameter is set to true,
        // so that openGL automatically converts our bytes to floats (same as ubyte/255.0f in vertex shader).

        // no need to unbind the vbo as we will bind another soon


        // init instancedVbo. this vbo's cursor will only get incremented when a new mesh instance is drawn.
        // typically, one object has a mesh for its own basis, and has its position in the whole world.
        // the instancedVbo will only pass one value for one instance (for more info, google vertexAttribDivisor).
        // this is the same as updating a uniform variable every instance, but with better performances.
        instancedVbo = glGenBuffers();
        bindVBO(instancedVbo);
        try (MemoryStack stack = MemoryStack.stackPush()){
            glBufferData(GL_ARRAY_BUFFER, wrap(genRandOffsets(instanceCount), stack), GL_STATIC_DRAW);
            // usually, you want it to be DYNAMIC_DRAW much like vbo. I was just lazy
        }

        // our buffer will contain a vec3 pos so 3 floats. pointer is 0 as we use a separate buffer for this.
        glVertexAttribPointer(2, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glVertexAttribDivisor(2, 1);    // tells openGL to update the buffer cursor for every instance
        // default is 0 (update every vertex), 2 is for every 2 instances.


        // init ubo
        ubo = glGenBuffers();
        bindUBO(ubo);

        // allocate storage for this buffer for passing data dynamically. // see sample.vs for why it's 72 bytes
        glBufferData(GL_UNIFORM_BUFFER, 72, GL_DYNAMIC_DRAW);

        unbindUBO();    // no longer need it bound
        glBindBufferBase(GL_UNIFORM_BUFFER, 0, ubo);    // tells openGL to attach ubo to "binding=0" in our shader




        uboDimensionNeedsUpdate = true;  // upload projection matrix at the beginning

        glBindVertexArray(0);   // unbinding VAO at the end is good practice as you often use several VAOs.
    }

    @Override
    public void loop(){
        while(window.live()){   // loop until window is manually closed by the user
            window.preRender();     // polling events (keyboard, mouse, window resizing...) & clear buffers

            shaderProgram.bind();   // binding shader before drawing

            glBindVertexArray(vao); // binding vao in order to use its vbos, ebo and ubo

            if (uboDimensionNeedsUpdate)
                updateProjectionMatrix(window.width, window.height);


            // bind vbo and fill it with vertex data dynamically (though here it doesn't change, for learning purposes)
            bindVBO(vbo);
            try(MemoryStack stack = MemoryStack.stackPush()){
                glBufferSubData(GL_ARRAY_BUFFER, 0, wrap(mesh.build(), stack)); // we override the whole buffer
                // so offset is 0
            }



            // vertex attrib array pointers are stored in the vao, so several vbos may have different pointers,
            // causing conflicts. Or more simple, you just want a vbo to use specific pointers, not just anything.
            // Thus, it's good practice to enable/disable pointers around draw call.
            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);


            bindVBO(instancedVbo);
            glEnableVertexAttribArray(2);


            // draw call
            glDrawElementsInstanced(GL_TRIANGLES, 36, GL_UNSIGNED_SHORT, 0, instanceCount);
            // ^ 0 is the offset at which openGL must start reading indices in the EBO to draw triangles
            // using GL_UNSIGNED_BYTE as data type for indices is usually not recommended for hardware reasons.
            // GL_UNSIGNED_SHORT is often the most compressed you can get


            glDisableVertexAttribArray(2);  // disabling vertex attrib pointers for good practice
            unbindVBO();    // really optional

            glDisableVertexAttribArray(0);
            glDisableVertexAttribArray(1);



            glBindVertexArray(0);   // unbinding vao after using its buffers

            ShaderProgram.unbind(); // unbinding shader after drawing

            window.postRender();    // swapping buffers
            Circe.debugGLErrors();    // debug, we never know
        }
    }

    @Override
    public void dispose(){
        // unbinding and deleting every OpenGL object

        unbindUBO();
        glDeleteBuffers(ubo);

        unbindEBO();
        glDeleteBuffers(ebo);

        unbindVBO();
        glDeleteBuffers(vbo);
        glDeleteBuffers(instancedVbo);

        glBindVertexArray(0);
        glDeleteVertexArrays(vao);
    }


    /*
    * Because I am too lazy to hardcode offsets myself.
    * */
    float[] genRandOffsets(int offsetCount){
        float[] f = new float[offsetCount * 3];

        Random r = new Random();

        for (int i=0; i < offsetCount; i++){
            int offset = i * 3;

            f[offset] = r.nextFloat() * i + 0.5f;
            f[offset + 1] = -r.nextFloat() * i - 1.5f;
            f[offset + 2] = -r.nextFloat() * i - 5f;
        }

        return f;
    }


    /**
     * A VAO must be bound before calling this
     * @param width widthIn
     * @param height heightIn
     */
    void updateProjectionMatrix(int width, int height){
        bindUBO(ubo);
        projectionMatrix = projectionMatrix.identity().perspective(
                fov, (float)width/height, 0.01f, 1000f);
        try(MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer fb = stack.mallocFloat(16); // matrix size
            projectionMatrix.get(fb);   // Matrix4f.get(Buffer) already does flip(), so do not call it, or it'll crash
            IntBuffer dim = stack.mallocInt(2);     // buffer for both width & height
            dim.put(width).put(height).flip();  // Alignment matches size for scalars,
            // so we can put the 2 ints next to each other

            glBufferSubData(GL_UNIFORM_BUFFER, 0, fb);
            glBufferSubData(GL_UNIFORM_BUFFER, 64, dim);
        }
        uboDimensionNeedsUpdate = false;
        unbindUBO();
    }

    public static void main(String[] args){
        new Sample().run();
    }
}

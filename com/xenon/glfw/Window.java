package com.xenon.glfw;

import com.xenon.glfw.abstraction.Disposable;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.system.MemoryStack.stackPush;

/**
 * Main window object used with GLFW
 * @author Zenon
 */
public class Window implements Disposable {

    public static Window build(String title, int width, int height){
        return new Window(title, width, height);
    }
    public static Window build(String title, int width, int height, GLFWFramebufferSizeCallback resizeCallback){
        return new Window(title, width, height, resizeCallback);
    }

    public final long handle;
    public int width, height;

    public Window(String title, int width, int height){

        handle = glfwCreateWindow(width, height, title, 0, 0);
        if ( handle == 0 )
            throw new IllegalStateException("GLFW failed to create the window.");

        glfwSetFramebufferSizeCallback(handle, new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                glViewport(0, 0, width, height);
                Window.this.width = width;
                Window.this.height = height;
            }
        });

        glfwMakeContextCurrent(handle);
        glfwSwapInterval(1);    // v-sync
        glfwShowWindow(handle);
        GL.createCapabilities();
        glClearColor(1, 1, 1, 1);
        this.width = width;
        this.height = height;
    }

    public Window(String title, int width, int height, GLFWFramebufferSizeCallback resizeCallback){

        handle = glfwCreateWindow(width, height, title, 0, 0);
        if ( handle == 0 )
            throw new IllegalStateException("GLFW failed to create the window.");

        glfwSetFramebufferSizeCallback(handle, resizeCallback);

        glfwMakeContextCurrent(handle);
        glfwSwapInterval(1);    // v-sync
        glfwShowWindow(handle);
        GL.createCapabilities();
        glClearColor(1, 1, 1, 1);
        this.width = width;
        this.height = height;
    }


    /**
     * Center the window
     */
    public void center(){
        GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        assert vidMode != null : "VidMode found to be null whilst centering the window "+handle;
        glfwSetWindowPos(
                handle,
                (vidMode.width() - width) / 2,
                (vidMode.height() - height) / 2
        );
    }

    /**
     * Set this window's icon
     * @param iconPath the path of the icon
     */
    public void setIcon(String iconPath){
        try ( MemoryStack stack = stackPush() ){
            IntBuffer w = stack.mallocInt(1);   // png width
            IntBuffer h = stack.mallocInt(1);   // png height
            IntBuffer comp = stack.mallocInt(1);// png components

            // desired_channels is 4 because we want to store Red, Green, Blue and Alpha components
            ByteBuffer icon = stbi_load(iconPath, w, h, comp, 4);
            assert icon != null : "stbi image loaded buffer found to be null whilst loading "+iconPath;

            glfwSetWindowIcon(handle, GLFWImage.malloc(1, stack)
                    .width(w.get(0))
                    .height(h.get(0))
                    .pixels(icon)
            );

            stbi_image_free(icon);
        }
    }

    /**
     * Polling pending events and clearing buffers
     */
    public void preRender(){
        glfwPollEvents();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    /**
     * Swapping frame buffer
     */
    public void postRender(){
        glfwSwapBuffers(handle);
    }

    /**
     *
     * @return whether this window is alive
     */
    public boolean live(){
        return !glfwWindowShouldClose(handle);
    }

    /**
     *
     * @param keyCode the given key code
     * @return whether the given key is pressed
     */
    public boolean isKeyPressed(int keyCode){
        return glfwGetKey(handle, keyCode) == GLFW_PRESS;
    }

    @Override
    public void dispose() {
        glfwFreeCallbacks(handle);
        glfwDestroyWindow(handle);
    }
}

package com.xenon.glfw;

import com.xenon.opengl.abstraction.Renderers;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.opengl.GL40.*;

/**
 * A few static helper methods for openGL
 * @author Zenon
 */
public class GLTools {

    /**
     * Direct-memory-allocation version of {@link ByteBuffer#wrap(byte[])}
     * @param bytes the array to put in a buffer
     * @param stack the memory stack with which the buffer lives
     * @return the corresponding created buffer, wrapping the array
     */
    public static ByteBuffer wrap(byte[] bytes, MemoryStack stack){
        return stack.malloc(bytes.length).put(bytes).flip();
    }
    /**
     * Direct-memory-allocation version of {@link IntBuffer#wrap(int[])}
     * @param ints the array to put in a buffer
     * @param stack the memory stack with which the buffer lives
     * @return the corresponding created buffer, wrapping the array
     */
    public static IntBuffer wrap(int[] ints, MemoryStack stack){
        return stack.mallocInt(ints.length).put(ints).flip();
    }
    /**
     * Direct-memory-allocation version of {@link FloatBuffer#wrap(float[])}
     * @param floats the array to put in a buffer
     * @param stack the memory stack with which the buffer lives
     * @return the corresponding created buffer, wrapping the array
     */
    public static FloatBuffer wrap(float[] floats, MemoryStack stack){
        return stack.mallocFloat(floats.length).put(floats).flip();
    }
    /**
     * Direct-memory-allocation version of {@link ShortBuffer#wrap(short[])}
     * @param shorts the array to put in a buffer
     * @param stack the memory stack with which the buffer lives
     * @return the corresponding created buffer, wrapping the array
     */
    public static ShortBuffer wrap(short[] shorts, MemoryStack stack){
        return stack.mallocShort(shorts.length).put(shorts).flip();
    }



    private static int program;
    /**
     * Essentially
     * <code>glUseProgram(id)</code>
     * @param id the shader program id to be bound
     */
    public static void bindProgram(int id) {
        if (id != program) {
            glUseProgram(id);
            program = id;
        }
    }
    /**
     * Essentially
     * <code>glUseProgram(0)</code>
     */
    public static void unbindProgram() {
        bindProgram(0);
    }

    private static int texture;
    /**
     * Essentially
     * <code>glBindTexture(GL_TEXTURE_2D, id)</code>
     * @param id the texture id to be bound
     */
    public static void bindTexture2D(int id) {
        if (id != texture) {
            glBindTexture(GL_TEXTURE_2D, id);
            texture = id;
        }
    }
    /**
     * Essentially
     * <code>glBindTexture(GL_TEXTURE_2D, 0)</code>
     */
    public static void unbindTexture2D() {
        bindTexture2D(0);
    }
    private static int vao;
    /**
     * Essentially
     * <code>glBindVertexArray(id)</code>
     * @param id the VAO id to be bound
     */
    public static void bindVAO(int id){
        if (id != vao) {
            glBindVertexArray(id);
            vao = id;
        }
    }
    /**
     * Essentially
     * <code>glBindVertexArray(0)</code>
     */
    public static void unbindVAO() {
        bindVAO(0);
    }

    private static int vbo;
    /**
     * Essentially
     * <code>glBindBuffer(GL_ARRAY_BUFFER, id)</code>
     * @param id the buffer's id to be bound
     */
    public static void bindVBO(int id){
        if (id != vbo) {
            glBindBuffer(GL_ARRAY_BUFFER, id);
            vbo = id;
        }
    }
    /**
     * Essentially
     * <code>glBindBuffer(GL_ARRAY_BUFFER, 0)</code>
     */
    public static void unbindVBO(){
        bindVBO(0);
    }

    private static int ebo;
    /**
     * Essentially
     * <code>glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, id)</code>
     * @param id the buffer's id to be bound
     */
    public static void bindEBO(int id){
        if (id != ebo) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, id);
            ebo = id;
        }
    }
    /**
     * Essentially
     * <code>glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)</code>
     */
    public static void unbindEBO(){
        bindEBO(0);
    }

    private static int ubo;
    /**
     * Essentially
     * <code>glBindBuffer(GL_UNIFORM_BUFFER, id)</code>
     * @param id the buffer's id to be bound
     */
    public static void bindUBO(int id){
        if (id != ubo) {
            glBindBuffer(GL_UNIFORM_BUFFER, id);
            ubo = id;
        }
    }
    /**
     * Essentially
     * <code>glBindBuffer(GL_UNIFORM_BUFFER, 0)</code>
     */
    public static void unbindUBO(){
        bindUBO(0);
    }

    private static int ibo;
    /**
     * Essentially
     * <code>glBindBuffer(GL_DRAW_INDIRECT_BUFFER, id)</code>
     * @param id the buffer's id to be bound
     */
    public static void bindIBO(int id){
        if (id != ibo) {
            glBindBuffer(GL_DRAW_INDIRECT_BUFFER, id);
            ibo = id;
        }
    }
    /**
     * Essentially
     * <code>glBindBuffer(GL_DRAW_INDIRECT_BUFFER, 0)</code>
     */
    public static void unbindIBO(){
        bindIBO(0);
    }

    /**
     * whether blending is currently enabled
     */
    public static boolean blend;

    /**
     * Essentially
     * <code>glEnable(GL_BLEND)</code>
     */
    public static void enableBlend() {
        if (!blend) {
            Renderers.draw();   // blending breaks batching
            glEnable(GL_BLEND);
            blend = true;
        }
    }

    /**
     * Essentially
     * <code>glDisable(GL_BLEND)</code>
     */
    public static void disableBlend() {
        if (blend) {
            glDisable(GL_BLEND);
            blend = false;
        }
    }

    /**
     * whether depth test is currently enabled
     */
    public static boolean depthTest;

    /**
     * Essentially
     * <code>glEnable(GL_DEPTH_TEST)</code>
     */
    public static void enableDepthTest() {
        if (!depthTest) {
            glEnable(GL_DEPTH_TEST);
            depthTest = true;
        }
    }

    /**
     * Essentially
     * <code>glDisable(GL_DEPTH_TEST)</code>
     */
    public static void disableDepthTest() {
        if(depthTest) {
            glDisable(GL_DEPTH_TEST);
            depthTest = false;
        }
    }

}

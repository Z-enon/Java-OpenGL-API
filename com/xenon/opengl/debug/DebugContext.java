package com.xenon.opengl.debug;


import com.xenon.glfw.GLFWContext;
import com.xenon.glfw.OpenGL;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Clean API from Vulkan introduced in GL43, against the deprecated error polling.
 * Statically typed.
 * @author Zenon
 * @see #createContext()
 */
public class DebugContext {

    /**
     * Creates a debug context, setting callbacks to print openGL logs.
     * The user can manually filter the logs by severity using
     * {@link org.lwjgl.opengl.GL43#glDebugMessageControl(int, int, int, int, boolean)}.
     */
    @OpenGL("Requires OpenGL 4.3")
    public static void createContext() {
        GLFWContext.requirement(4, 3);
        glEnable(GL_DEBUG_OUTPUT);
        glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);
        glDebugMessageCallback(new GLDebugMessageCallback() {
            @Override
            public void invoke(int source, int type, int id, int severity, int length, long message, long userParam) {
                logOpenGLMSG(source, type, id, severity, length, message);
            }
        }, NULL);
    }

    /**
     * Kills the current debug context. It is actually just hiding some <code>glDisable</code> so it won't crash
     * if no context has been created beforehand.
     */
    @OpenGL("Requires OpenGL 4.3")
    public static void killContext() {
        glDisable(GL_DEBUG_OUTPUT_SYNCHRONOUS);
        glDisable(GL_DEBUG_OUTPUT);
    }

    private static void logOpenGLMSG(int source, int type, int id, int severity, int length, long msg_address) {
        String log = "[severity: " + switch (severity) {
            case GL_DEBUG_SEVERITY_HIGH -> "HIGH";
            case GL_DEBUG_SEVERITY_MEDIUM -> "MEDIUM";
            case GL_DEBUG_SEVERITY_LOW -> "LOW";
            case GL_DEBUG_SEVERITY_NOTIFICATION -> "INFO";
            default -> throw new AssertionError("Just can't be happening");
        } +
                "] From source: " +
                switch (source) {
                    case GL_DEBUG_SOURCE_API -> "API";
                    case GL_DEBUG_SOURCE_WINDOW_SYSTEM -> "WINDOW";
                    case GL_DEBUG_SOURCE_SHADER_COMPILER -> "SHADER COMPILER";
                    case GL_DEBUG_SOURCE_THIRD_PARTY -> "THIRD PARTY";
                    case GL_DEBUG_SOURCE_APPLICATION -> "APP";
                    case GL_DEBUG_SOURCE_OTHER -> "OTHER";
                    default -> throw new AssertionError("Just can't be happening");
                } +
                " cause: " +
                switch (type) {
                    case GL_DEBUG_TYPE_ERROR -> "ERROR";
                    case GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR -> "DEPRECATED BEHAVIOUR";
                    case GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR -> "UNDEFINED BEHAVIOUR";
                    case GL_DEBUG_TYPE_PORTABILITY -> "PORTABILITY";
                    case GL_DEBUG_TYPE_PERFORMANCE -> "PERFORMANCE";
                    case GL_DEBUG_TYPE_OTHER -> "OTHER";
                    case GL_DEBUG_TYPE_MARKER -> "MARKER";
                    default -> throw new AssertionError("Just can't be happening");
                } +
                " as ID: " + id +
                " description: " + MemoryUtil.memUTF8(msg_address, length);
        System.out.println(log);
    }

}

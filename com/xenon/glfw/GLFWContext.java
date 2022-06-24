package com.xenon.glfw;

import com.xenon.glfw.abstraction.Disposable;
import org.lwjgl.glfw.GLFWErrorCallback;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_TRUE;

/**
 * Hold information about openGL version major and minor and the core profile as well as the corresponding glsl version.
 * @author Zenon
 */
public final class GLFWContext implements Disposable {

    private static GLFWContext currentContext;

    /**
     *
     * @return the current GLFWContext, if any
     * @throws RuntimeException if no GLFWContext is currently alive
     */
    public static GLFWContext current() {
        if (currentContext == null)
            throw new RuntimeException("No GLFWContext currently alive! Create one before trying to access it.");
        return currentContext;
    }

    /**
     * Checks whether the current context meets the requirements
     * @param major the version major
     * @param minor the version minor
     * @throws RuntimeException if the current context does not match the requirements
     */
    public static void requirement(int major, int minor) {
        if (!currentContext.isAtLeast(major, minor))
            throw new RuntimeException("OpenGL requirements not met: required version "+major+'.'+minor+
                    ". Current version "+currentContext.versionMajor+'.'+currentContext.versionMinor);
    }

    /**
     * Start GLFW and return a simple <code>GLFWContext</code> object for further use.
     * @param versionMajor the major version of GLFW
     * @param versionMinor the minor version of GLFW
     * @param coreVersion whether GLFW version should be core profile
     * @return a new <code>GLFWContext</code> object for further use
     * @throws RuntimeException if there already is a GLFWContext alive
     */
    public static GLFWContext build(int versionMajor, int versionMinor, boolean coreVersion){
        if (currentContext != null)
            throw new RuntimeException("Already have a GLFWContext alive!" +
                    " Dispose of the first one before creating another.");
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("GLFW failed to initialize.");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, versionMajor);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, versionMinor);
        if (coreVersion)
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        return currentContext = new GLFWContext(versionMajor, versionMinor, coreVersion);
    }

    public final int versionMajor, versionMinor, glslVersion;
    public final boolean coreVersion;

    private GLFWContext(int versionMajor, int versionMinor, boolean coreVersion){
        this.versionMajor = versionMajor;
        this.versionMinor = versionMinor;
        this.coreVersion = coreVersion;

        if (versionMajor >= 3 && (versionMajor >= 4 || versionMinor >= 3))
            glslVersion = versionMajor * 100 + versionMinor * 10;
        else{
            int version;

            if (versionMajor == 2){
                version = switch (versionMinor){
                    case 0 -> 110;
                    case 1 -> 120;
                    default -> throw new IllegalArgumentException("Illegal openGL version: "+
                            versionMajor+"."+versionMinor);
                };
            }else if (versionMajor == 3){
                version = switch (versionMinor){
                    case 0 -> 130;
                    case 1 -> 140;
                    case 2 -> 150;
                    default -> throw new IllegalArgumentException("Illegal openGL version: "+
                            versionMajor+"."+versionMinor);
                };
            }else
                throw new IllegalArgumentException("Illegal openGL version: "+versionMajor+"."+versionMinor);

            glslVersion = version;
        }

    }

    /**
     * Compares the given version with this instance.
     * @param major the version major
     * @param minor the version minor
     * @return whether this version is at least the version passed in arguments
     */
    public boolean isAtLeast(int major, int minor) {
        return (versionMajor == major && versionMinor >= minor) || versionMajor > major;
    }


    @Override
    @SuppressWarnings("all")
    public void dispose() {
        currentContext = null;
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
}

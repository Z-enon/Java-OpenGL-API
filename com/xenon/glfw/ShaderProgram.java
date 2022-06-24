package com.xenon.glfw;

import com.xenon.glfw.abstraction.Disposable;

import java.util.AbstractMap;

import static com.xenon.glfw.GLTools.bindProgram;
import static com.xenon.glfw.GLTools.unbindProgram;
import static org.lwjgl.opengl.GL20.*;

/**
 * @author Zenon
 */
public class ShaderProgram implements Disposable {

    /**
     * Create a new shader program, as well as its vertex and fragment subprograms, then link everything.
     * @param shadersCode  the vertex and shader code
     */
    public static ShaderProgram build(AbstractMap.SimpleEntry<String, String> shadersCode){
        return new ShaderProgram(shadersCode.getKey(), shadersCode.getValue());
    }


    /**
     * Create a new shader program, as well as its vertex and fragment subprograms, then link everything.
     * @param vertexCode the vertex shader source code without the version header
     * @param fragmentCode the fragment shader source code without the version header
     */
    public static ShaderProgram build(String vertexCode, String fragmentCode){
        return new ShaderProgram(vertexCode, fragmentCode);
    }


    public final int programId;
    /**
     * Create a new shader program, as well as its vertex and fragment subprograms, then link everything.
     * @param vertexCode the vertex shader source code without the version header
     * @param fragmentCode the fragment shader source code without the version header
     */
    public ShaderProgram(String vertexCode, String fragmentCode){
        programId = glCreateProgram();
        if ( programId == 0 )
            throw new RuntimeException("Failed to create a shader program of code "+vertexCode+", \n"+fragmentCode);
        GLFWContext context = GLFWContext.current();
        String header = "#version " + context.glslVersion + (context.coreVersion ? " core\n" : "\n");

        int vertexId = createShader(header + vertexCode, GL_VERTEX_SHADER);
        int fragmentId = createShader(header + fragmentCode, GL_FRAGMENT_SHADER);
        // linking
        glLinkProgram(programId);
        if ( glGetProgrami(programId, GL_LINK_STATUS) == 0 )
            throw new RuntimeException("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
        // no longer need vertex and fragment shader objects
        glDetachShader(programId, vertexId);
        glDetachShader(programId, fragmentId);
        glValidateProgram(programId);
        if ( glGetProgrami(programId, GL_VALIDATE_STATUS) == 0 )
            System.err.println("Warning validating Shader code: " + glGetProgramInfoLog(programId, 1024));
    }

    /**
     * Create a new shader and return its pointer. Only used in constructor.
     * programId must have been initialized before calling this method.
     * @see #ShaderProgram(String, String)
     * @param code the shader's code
     * @param type the type of the shader. Either {@link org.lwjgl.opengl.GL20#GL_VERTEX_SHADER} or
     *             {@link org.lwjgl.opengl.GL20#GL_FRAGMENT_SHADER}.
     * @return the pointer of the newly created shader
     */
    private int createShader(String code, int type){
        int id = glCreateShader(type);
        if ( id == 0 )
            throw new RuntimeException("Failed to create a shader of type " + type + ", with code " + code);

        glShaderSource(id, code);
        glCompileShader(id);

        if ( glGetShaderi(id, GL_COMPILE_STATUS) == 0 )
            throw new RuntimeException("Error compiling Shader code: " + glGetShaderInfoLog(id, 1024));

        glAttachShader(programId, id);
        return id;
    }


    /**
     * Bind this shader program.
     */
    public void bind(){
        bindProgram(programId);
    }

    /**
     * Unbind the currently bound shader program.
     */
    public static void unbind(){
        unbindProgram();
    }

    @Override
    public void dispose() {
        unbind();
        glDeleteProgram(programId);
    }
}

package com.xenon.opengl.debug;

import com.xenon.glfw.GLFWContext;
import com.xenon.glfw.ModelUtils;
import com.xenon.glfw.ShaderProgram;
import com.xenon.glfw.Window;
import com.xenon.glfw.abstraction.App;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.AbstractMap;
import java.util.Random;

/**
 * @author Zenon
 */
public abstract class TestUnit extends Constants implements App {

    protected final Window window;
    protected final ShaderProgram shader;

    protected static void print(Object... objects) {
        for (var o : objects)
            System.out.println(o);
    }

    protected static void quickSample(ByteBuffer bb, int quadCount) {
        Random r = new Random();
        float offset = 0;
        for (int i = 0; i < quadCount; i++) {
            byte re = (byte) r.nextInt(256);
            byte g = (byte) r.nextInt(256);
            byte b = (byte) r.nextInt(256);
            bb.putFloat(-1 + offset).putFloat(1 - offset).put(re).put(g).put(b).put((byte) 255);
            bb.putFloat(-1 + offset).putFloat(-offset).put(re).put(g).put(b).put((byte) 255);
            bb.putFloat(offset).putFloat(-offset).put(re).put(g).put(b).put((byte) 255);
            bb.putFloat(offset).putFloat(1 - offset).put(re).put(g).put(b).put((byte) 255);
            offset += r.nextFloat();
        }
        bb.flip();
    }

    protected static ShortBuffer quickIndices(MemoryStack stack, int quadCount) {
        short[] indices = ModelUtils.genQuadIndicesS(quadCount);
        ShortBuffer bb = stack.mallocShort(indices.length);
        return bb.put(indices).flip();
    }

    protected TestUnit(int width, int height, GLFWContext context, AbstractMap.SimpleEntry<String, String> shaders) {
        this(width, height, context, shaders.getKey(), shaders.getValue());
    }
    @SuppressWarnings("unused")
    protected TestUnit(int width, int height, GLFWContext context, String ver, String frag) {
        window = Window.build("", width, height);
        window.center();

        shader = ShaderProgram.build(ver, frag);
        DebugContext.createContext();
    }

    @Override
    public void loop() {
        while (window.live()) {
            window.preRender();

            shader.bind();
            draw();

            window.postRender();
        }
    }

    protected abstract void draw();

    @Override
    public void dispose() {
        shader.dispose();
        window.dispose();
        GLFWContext.current().dispose();
    }
}

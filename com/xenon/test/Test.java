package com.xenon.test;

import com.xenon.glfw.GLFWContext;
import com.xenon.glfw.GLTools;
import com.xenon.glfw.Window;
import com.xenon.glfw.abstraction.App;
import com.xenon.opengl.abstraction.Renderers;
import com.xenon.opengl.debug.DebugContext;
import com.xenon.ui.UIContexts;
import com.xenon.ui.abstraction.UIContext;
import org.lwjgl.opengl.GL11;

/**
 * @author Zenon
 */
public class Test implements App {

    public static void main(String[] a){
        new Test().run();
    }

    Window window;

    @Override
    public void init() {
        GLFWContext.build(4, 6, true);


        final int width = 800, height = 500;

        window = Window.build("", width, height);
        window.center();
        DebugContext.createContext();

        Renderers.init(width, height, 4, 4, 4);
        GLTools.enableDepthTest();
    }

    @Override
    public void loop() {
        while(window.live()) {
            window.preRender();

            Renderers.zlevel(100);
            UIContext c = UIContexts.disableBounds();
            c.begin(Renderers.POS2_COL);
            c.drawColoredRect(0, 0, 700, 400, 0xFF00FF98);
            Renderers.zlevel(150);
            c.drawColoredRect(500, 350, 250, 100, 0xFF000000);
            Renderers.POS2_COL.GPU();
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            c.drawColoredRect(0, 0, 800, 500, 0x50FF0000);
            Renderers.POS2_COL.GPU();
            GL11.glDisable(GL11.GL_BLEND);

            window.postRender();
        }
    }

    @Override
    public void dispose() {
        Renderers.dispose();
        window.dispose();
        GLFWContext.current().dispose();
    }


}

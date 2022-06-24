package com.xenon.glfw.abstraction;

/**
 * @author Zenon
 */
public interface App extends LifeCycle{

    void loop();

    default void run(){
        init();
        try {
            loop();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            dispose();
        }
    }

}

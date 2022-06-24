package com.xenon.glfw.abstraction;

import org.joml.Vector3f;

/**
 * @author Zenon
 */
public interface Model {

    /**
     *
     * @return The model's vertex data
     */
    float[] getVertices();

    /**
     *
     * @return The model's rotation
     */
    Vector3f getRotation();

    /**
     *
     * @return The model's scale
     */
    float getScale();

}

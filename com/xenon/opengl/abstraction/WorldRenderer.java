package com.xenon.opengl.abstraction;

import com.xenon.glfw.ShaderProgram;
import com.xenon.glfw.abstraction.Disposable;
import com.xenon.opengl.VertexFormat;

/**
 * @author Zenon
 */
public interface WorldRenderer extends Disposable {

    void build(ShaderProgram attachedProgram);
    VertexFormat format();
    WorldRenderer pos(double x, double y);
    WorldRenderer pos(double x, double y, double z);
    WorldRenderer tex(double u, double v);
    WorldRenderer color(int r, int g, int b, int a);
    void endVertex();
    void GPU();
}

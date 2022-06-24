package com.xenon.ui.abstraction;

import com.xenon.opengl.abstraction.WorldRenderer;

/**
 * Drawing context for UIs. Destined to be a simple wrapper for mouse coordinates,
 * it turned out to also support boundaries-restricted draws. See {@link com.xenon.ui.UIContexts}.
 * Common mistakes:
 * <ul>
 *     <li>forget to call {@link #begin(WorldRenderer)} before calling any of the draw functions</li>
 *     <li>forget to call {@link #update(int, int)} during GLFW events polling</li>
 *     <li>Mistakes regarding canvas/drawing in boundaries, see {@link com.xenon.ui.UIContexts}</li>
 * </ul>
 * @author Zenon
 * @see com.xenon.ui.UIContexts
 */
public interface UIContext {

    /**
     * Updates the new mouse coordinates
     * @param mouseX the mouse's x
     * @param mouseY the mouse's y
     */
    void update(int mouseX, int mouseY);

    /**
     * @return mouse's x
     */
    int mouseX();

    /**
     * @return mouse's y
     */
    int mouseY();

    /**
     * Sets new bounds to the current frame, by defining an upper-left corner and a bottom-right corner.
     * @param x1 upper-left corner's x
     * @param y1 upper-left corner's y
     * @param x2 bottom-right corner's x
     * @param y2 bottom-right corner's y
     */
    void setBounds(int x1, int y1, int x2, int y2);

    /**
     * Essentially
     * <code>setBounds(x1, y1, x1 + width, y1 + height)</code>.
     * @param x1 upper-left corner's x
     * @param y1 upper-left corner's y
     * @param width the canvas' width
     * @param height the canvas' height
     * @see #setBounds(int, int, int, int)
     */
    default void setBoundsByDimensions(int x1, int y1, int width, int height) {
        setBounds(x1, y1, x1 + width, y1 + height);
    }

    /**
     * Makes future calls use the given WorldRenderer.
     * @param renderer the new WorldRenderer
     */
    void begin(WorldRenderer renderer);

    /**
     * Draws a colored rectangle.
     * @param x the rectangle's upper-left corner's x
     * @param y the rectangle's upper-left corner's y
     * @param width the rectangle's width
     * @param height the rectangle's height
     * @param color the color of the rectangle
     * @see com.xenon.opengl.RenderUtils#drawColoredRect(WorldRenderer, double, double, double, double, int)
     */
    void drawColoredRect(double x, double y, double width, double height, int color);

    /**
     * Draws a textured rectangle by rescaling uvs according to the texture's width and height.
     * @param x the rectangle's upper-left corner's x
     * @param y the rectangle's upper-left corner's y
     * @param u the texture's upper-left corner's u (in pixels)
     * @param v the texture's upper-left corner's v (in pixels)
     * @param width the rectangle's width
     * @param height the rectangle's height
     * @param tex_width the texture's width
     * @param tex_height the texture's height
     * @see com.xenon.opengl.RenderUtils#drawTexturedRect(WorldRenderer, double, double, double,
     * double, double, double, double, double)
     */
    void drawTexturedRect(double x, double y, double u, double v,
                          double width, double height, double tex_width, double tex_height);

    /**
     * Draws a colored textured rectangle by rescaling uvs according to the texture's width and height.
     * @param x the rectangle's upper-left corner's x
     * @param y the rectangle's upper-left corner's y
     * @param u the texture's upper-left corner's u (in pixels)
     * @param v the texture's upper-left corner's v (in pixels)
     * @param width the rectangle's width
     * @param height the rectangle's height
     * @param tex_width the texture's width
     * @param tex_height the texture's height
     * @param color the color of the rectangle
     * @see com.xenon.opengl.RenderUtils#drawTexturedColoredRect(WorldRenderer, double, double, double, double,
     * double, double, double, double, int)
     */
    void drawTexturedColoredRect(double x, double y, double u, double v,
                                 double width, double height, double tex_width, double tex_height,
                                 int color);

    /**
     * Draws 4 rectangles to render a bounding rectangle.
     * @param x the boundaries' upper-left corner's x
     * @param y the boundaries' upper-left corner's y
     * @param width the boundaries width
     * @param height the boundaries height
     * @param thickness the thickness of the bounding rectangle
     * @param color the color of the bounding rectangle
     */
    void drawBoundingRect(double x, double y, double width, double height, double thickness, int color);
}

package com.xenon.ui;

import com.xenon.opengl.RenderUtils;
import com.xenon.opengl.abstraction.WorldRenderer;
import com.xenon.ui.abstraction.UIContext;

/**
 * @author Zenon
 */
public class UIContexts {

    /*Singletons*/
    private static final UIContext baseHandlerSingleton = new BaseUIContext();
    private static final UIContext frameHandlerSingleton = new FrameUIContext();

    /*Current WorldRenderer instance for both UIContext singletons*/
    private static WorldRenderer currentRenderer;

    /**
     * Updates the new mouse's coordinates to the UIHandler singletons.
     * @param mouseX the mouse's x
     * @param mouseY the mouse's y
     * @see BaseUIContext#update(int, int)
     */
    public static void updateMouse(int mouseX, int mouseY) {
        baseHandlerSingleton.update(mouseX, mouseY);
        frameHandlerSingleton.update(mouseX, mouseY);
    }

    /**
     * Enables frame bounds, returning the correct UIHandler instance to work with.
     * @param x1 upper-left corner's x
     * @param y1 upper-left corner's y
     * @param width the canvas' width
     * @param height the canvas' height
     * @return the UIHandler singleton for handling bounds
     * @see FrameUIContext
     */
    public static UIContext enableBounds(int x1, int y1, int width, int height) {
        frameHandlerSingleton.setBoundsByDimensions(x1, y1, width, height);
        return frameHandlerSingleton;
    }

    /**
     * Returns the correct UIHandler instance to NOT work with boundaries.
     * 2D coordinates are sent raw to the WorldRenderer.
     * @return the UIHandler singleton for NOT handling bounds
     * @see BaseUIContext
     */
    public static UIContext disableBounds() {
        return baseHandlerSingleton;
    }

    private static class BaseUIContext implements UIContext {

        protected int mouseX, mouseY;

        @Override
        public void update(int mouseX, int mouseY) {
            this.mouseX = mouseX;
            this.mouseY = mouseY;
        }

        @Override
        public int mouseX() {
            return mouseX;
        }

        @Override
        public int mouseY() {
            return mouseY;
        }

        @Override
        public void setBounds(int x1, int y1, int x2, int y2) {
            throw new UnsupportedOperationException("Cannot set bounds with a BaseUIHandler instance." +
                    " Consider using UIHandlers.enableBounds(int...).");
        }

        @Override
        public void begin(WorldRenderer renderer) {
            if (currentRenderer != renderer)
                currentRenderer = renderer;
        }

        /*      | Default is just to delegate to RenderUtils |      */

        @Override
        public void drawColoredRect(double x, double y, double width, double height, int color) {
            RenderUtils.drawColoredRect(currentRenderer, x, y, width, height, color);
        }

        @Override
        public void drawTexturedRect(double x, double y, double u, double v,
                                     double width, double height, double tex_width, double tex_height) {
            RenderUtils.drawTexturedRect(currentRenderer, x, y, u, v, width, height, tex_width, tex_height);
        }

        @Override
        public void drawTexturedColoredRect(double x, double y, double u, double v,
                                            double width, double height, double tex_width, double tex_height,
                                            int color) {
            RenderUtils.drawTexturedColoredRect(currentRenderer, x, y, u, v, width, height, tex_width, tex_height, color);
        }

        @Override
        public void drawBoundingRect(double x, double y, double width, double height, double thickness, int color) {
            RenderUtils.drawBoundingRect(currentRenderer, x, y, width, height, thickness, color);
        }
    }
    private static class FrameUIContext extends BaseUIContext {
        /*
        * The frame's AABB boundaries
        * */
        private int x1, y1, x2, y2;

        @Override
        public void setBounds(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        /*      | Boundaries checking algorithms |      */

        @Override
        public void drawColoredRect(double x, double y, double width, double height, int color) {
            drawColoredRectRaw(x, y, x + width, y + height, color);
        }

        /**
         * Made it a separated method for reuse in {@link #drawBoundingRect(double, double, double, double, double, int)}
         */
        private void drawColoredRectRaw(double _x1, double _y1, double _x2, double _y2, int color) {
            if (_x1 >= x2 || _x2 <= x1 || _y1 >= y2 || _y2 <= y1)   return;

            _x1 = Math.max(_x1, x1);
            _y1 = Math.max(_y1, y1);
            _x2 = Math.min(_x2, x2);
            _y2 = Math.min(_y2, y2);
            RenderUtils.drawColoredRectRaw(currentRenderer, _x1, _y1, _x2, _y2, color);
        }

        @Override
        public void drawTexturedRect(double x, double y, double u, double v,
                                     double width, double height, double tex_width, double tex_height) {
            double r_x1 = x, r_y1 = y, u1 = u, v1 = v, u2 = u + width, v2 = v + height;
            double r_x2 = x + width;
            double r_y2 = y + width;
            if (x >= x2 || r_x2 <= x1 || y >= y2 || r_y2 <= y1)   return;

            if (x <= x1) {
                u1 += x1 - x;
                r_x1 = x1;
            }
            if (r_x2 >= x2) {
                u2 -= r_x2 - x2;
                r_x2 = x2;
            }
            if (y <= y1) {
                v1 += y1 - y;
                r_y1 = y1;
            }
            if (r_y2 >= y2) {
                v2 -= r_y2 - y2;
                r_y2 = y2;
            }
            u1 /= tex_width;
            u2 /= tex_width;
            v1 /= tex_height;
            v2 /= tex_height;
            RenderUtils.drawTexturedRectRaw(currentRenderer, r_x1, r_y1, u1, v1, r_x2, r_y2, u2, v2);
        }

        @Override
        public void drawTexturedColoredRect(double x, double y, double u, double v,
                                            double width, double height, double tex_width, double tex_height,
                                            int color) {
            double r_x1 = x, r_y1 = y, u1 = u, v1 = v, u2 = u + width, v2 = v + height;
            double r_x2 = x + width;
            double r_y2 = y + width;
            if (x >= x2 || r_x2 <= x1 || y >= y2 || r_y2 <= y1)   return;

            if (x <= x1) {
                u1 += x1 - x;
                r_x1 = x1;
            }
            if (r_x2 >= x2) {
                u2 -= r_x2 - x2;
                r_x2 = x2;
            }
            if (y <= y1) {
                v1 += y1 - y;
                r_y1 = y1;
            }
            if (r_y2 >= y2) {
                v2 -= r_y2 - y2;
                r_y2 = y2;
            }
            u1 /= tex_width;
            u2 /= tex_width;
            v1 /= tex_height;
            v2 /= tex_height;
            RenderUtils.drawTexturedColoredRectRaw(currentRenderer, r_x1, r_y1, u1, v1, r_x2, r_y2, u2, v2, color);
        }

        /*
        * Basically RenderUtils.drawBoundingRect() reimplemented with bounds checking function this.drawColoredRectRaw()
        * */
        @Override
        public void drawBoundingRect(double x, double y, double width, double height, double thickness, int color) {
            double ulx = x - thickness;
            double uly = y - thickness;
            double x2 = x + width;
            double y2 = y + height;
            double brx = x2 + thickness;
            double bry = y2 + thickness;
            drawColoredRectRaw(ulx, uly, x2, y, color);  // top
            drawColoredRectRaw(ulx, y, x, bry, color);   // left
            drawColoredRectRaw(x, y2, brx, bry, color);  // bottom
            drawColoredRectRaw(x2, uly, brx, y2, color); // right
        }
    }
}

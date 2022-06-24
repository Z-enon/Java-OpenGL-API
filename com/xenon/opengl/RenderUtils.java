package com.xenon.opengl;

import com.xenon.opengl.abstraction.WorldRenderer;

public class RenderUtils {

    /*Basics methods*/

    public static void drawColoredRect(WorldRenderer w, double x, double y, double width, double height, int color) {
        drawColoredRectRaw(w, x, y, x + width, y + height, color);
    }
    public static void drawColoredRectRaw(WorldRenderer w, double x1, double y1, double x2, double y2, int color) {
        int r = color >> 16 & 255;
        int g = color >> 8 & 255;
        int b = color & 255;
        int a = color >> 24 & 255;

        w.pos(x1, y1).color(r, g, b, a).endVertex();
        w.pos(x1, y2).color(r, g, b, a).endVertex();
        w.pos(x2, y2).color(r, g, b, a).endVertex();
        w.pos(x2, y1).color(r, g, b, a).endVertex();
    }

    public static void drawTexturedRect(WorldRenderer w, double x, double y, double u, double v,
                                        double width, double height, double texWidth, double texHeight) {
        double u1 = u / texWidth;
        double v1 = v / texHeight;
        double u2 = (u + width) / texWidth;
        double v2 = (v + height) / texHeight;
        drawTexturedRectRaw(w, x, y, u1, v1, x + width, y + height, u2, v2);
    }

    public static void drawTexturedRectRaw(WorldRenderer w, double x1, double y1, double u1, double v1,
                                        double x2, double y2, double u2, double v2) {

        w.pos(x1, y1).tex(u1, v1).endVertex();
        w.pos(x1, y2).tex(u1, v2).endVertex();
        w.pos(x2, y2).tex(u2, v2).endVertex();
        w.pos(x2, y1).tex(u2, v1).endVertex();
    }
    public static void drawTexturedColoredRect(WorldRenderer w, double x, double y, double u, double v,
                                               double width, double height, double texWidth, double texHeight,
                                               int color) {
        double u1 = u / texWidth;
        double v1 = v / texHeight;
        double u2 = (u + width) / texWidth;
        double v2 = (v + height) / texHeight;
        drawTexturedColoredRectRaw(w, x, y, u1, v1, x + width, y + height, u2, v2, color);
    }

    public static void drawTexturedColoredRectRaw(WorldRenderer w, double x1, double y1, double u1, double v1,
                                                  double x2, double y2, double u2, double v2, int color) {
        int r = color >> 16 & 255;
        int g = color >> 8 & 255;
        int b = color & 255;
        int a = color >> 24 & 255;

        w.pos(x1, y1).tex(u1, v1).color(r, g, b, a).endVertex();
        w.pos(x1, y2).tex(u1, v2).color(r, g, b, a).endVertex();
        w.pos(x2, y2).tex(u2, v2).color(r, g, b, a).endVertex();
        w.pos(x2, y1).tex(u2, v1).color(r, g, b, a).endVertex();
    }

    /*More advanced methods*/

    public static void drawBoundingRect(WorldRenderer w, double x, double y, double width, double height,
                                        double thickness, int color) {
        double ulx = x - thickness;
        double uly = y - thickness;
        double x2 = x + width;
        double y2 = y + height;
        double brx = x2 + thickness;
        double bry = y2 + thickness;
        drawColoredRectRaw(w, ulx, uly, x2, y, color);  // top
        drawColoredRectRaw(w, ulx, y, x, bry, color);   // left
        drawColoredRectRaw(w, x, y2, brx, bry, color);  // bottom
        drawColoredRectRaw(w, x2, uly, brx, y2, color); // right
    }

}

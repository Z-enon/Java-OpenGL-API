package com.xenon.font;

import com.xenon.glfw.abstraction.Disposable;
import com.xenon.opengl.RenderUtils;
import com.xenon.opengl.abstraction.WorldRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.xenon.glfw.GLTools.bindTexture2D;
import static org.lwjgl.opengl.GL40.*;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

public class GLFont implements Disposable {

    public static GLFont createFont(String name, int size, boolean antialiasing) {
        return new GLFont(new Font(name, Font.PLAIN, size), antialiasing);
    }
    public static GLFont createFont(Path ttf, int size, boolean antialiasing) {
        Font font;
        try (InputStream in = Files.newInputStream(ttf)) {
            font = Font.createFont(Font.TRUETYPE_FONT, in).deriveFont(Font.PLAIN, size);
        } catch (IOException | FontFormatException e) {
            throw new IllegalStateException(e);
        }
        return new GLFont(font, antialiasing);
    }

    protected static GLFont currentFont;

    public static WorldRenderer FONT_FORMAT;

    public static void init() {
        /*
        FONT_FORMAT = new FontRenderer(20);
        try {
            FONT_FORMAT.buildShaderProgram(
                    Files.readString(Paths.get("./src/com/xenon/font/font.vs")),
                    Files.readString(Paths.get("./src/com/xenon/font/font.fs")),
                    context
            );
        } catch(IOException e) {
            throw new IllegalStateException(e);
        }
        DefaultRenderers.registerFormats(FONT_FORMAT);*/    //TODO
    }


    protected final int[] char_widths = new int[224];
    protected final int[] char_x_coordinates = new int[224];

    public final int font_height, font_width;
    public final int texture;

    public GLFont(Font font, boolean antialiasing) {
        //
        // https://github.com/SilverTiger/lwjgl3-tutorial/wiki/Fonts
        Graphics2D g;

        g = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
        if (antialiasing)
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        FontMetrics metrics = g.getFontMetrics(font);
        g.dispose();

        int atlas_width = 0;

        for (int i=32; i < 256; i++)
            if (i != 127)
                atlas_width += (char_widths[i - 32] = metrics.charWidth(i));

        font_width = atlas_width;
        font_height = metrics.getHeight();

        BufferedImage atlas = new BufferedImage(font_width, font_height, BufferedImage.TYPE_INT_ARGB);
        g = atlas.createGraphics();
        if (antialiasing)
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x = 0;
        for (int i=32; i < 256; i++) {
            if (i != 127) {
                char_x_coordinates[i - 32] = x;

                g.setFont(font);
                g.setPaint(Color.WHITE);
                g.drawString(String.valueOf((char)i), x, metrics.getAscent());
                x += char_widths[i - 32];
            }
        }
        g.dispose();

        int[] colors = atlas.getRGB(0, 0, font_width, font_height, null, 0, font_width);

        ByteBuffer pixels = memAlloc(font_width * font_height * 4);
        for (int c : colors) {
            pixels.put((byte) (c >> 16 & 255));
            pixels.put((byte) (c >> 8 & 255));
            pixels.put((byte) (c & 255));
            pixels.put((byte) (c >> 24 & 255));
        }
        pixels.flip();

        texture = glGenTextures();
        bindTexture2D(texture);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, font_width, font_height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
        memFree(pixels);
    }

    public void drawString(String s, float x, float y, int color) {
        WorldRenderer r = changeFontIfNeeded();

        for (char c : s.toCharArray())
            if (isASCII(c)) {
                int width = char_widths[c - 32];
                RenderUtils.drawTexturedColoredRect(
                        r, x, y,
                        char_x_coordinates[c - 32], 0,
                        width, font_height,
                        font_width, font_height,
                        color
                );
                x += width;
            }
    }

    public int getStringWidth(String s) {
        int w = 0;
        for (char c : s.toCharArray())
            if (isASCII(c))
                w += char_widths[c - 32];
        return w;
    }

    protected WorldRenderer changeFontIfNeeded() {
        WorldRenderer r = FONT_FORMAT;
        if (currentFont != this) {
            r.GPU();
            bindTexture2D(this.texture);
            currentFont = this;
        }
        return r;
    }


    public static boolean isASCII(char c) {
        return c != 127 && 32 <= c && c <= 255;
    }

    @Override
    public void dispose() {
        glDeleteTextures(texture);
    }
}

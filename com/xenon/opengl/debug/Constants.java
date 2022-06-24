package com.xenon.opengl.debug;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL40.GL_DRAW_INDIRECT_BUFFER;

/**
 * @author Zenon
 */
public class Constants {

    public static final int s_sh = Short.BYTES, s_in = Integer.BYTES, s_flt = Float.BYTES, s_dbl = Double.BYTES;

    public static final int tgl = GL_TRIANGLES,
            tgl_strip = GL_TRIANGLE_STRIP;
    public static final int ch = GL_BYTE, uch = GL_UNSIGNED_BYTE,
            sh = GL_SHORT, ush = GL_UNSIGNED_SHORT,
            in = GL_INT, uin = GL_UNSIGNED_INT,
            flt = GL_FLOAT,
            dbl = GL_DOUBLE;

    public static final int stc = GL_STATIC_DRAW, dyn = GL_DYNAMIC_DRAW;

    public static final int arr_buf = GL_ARRAY_BUFFER,
            el_buf = GL_ELEMENT_ARRAY_BUFFER,
            ind_buf = GL_DRAW_INDIRECT_BUFFER;


}

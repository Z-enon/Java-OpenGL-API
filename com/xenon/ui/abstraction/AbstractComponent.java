package com.xenon.ui.abstraction;

public abstract class AbstractComponent implements Drawable{

    public final int x, y, width, height;

    public AbstractComponent(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

}

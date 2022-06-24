package com.xenon.ui.abstraction;


public abstract class AbstractButton extends AbstractComponent implements Clickable, Hoverable{

    protected final Runnable onClick;

    public AbstractButton(int x, int y, int width, int height, Runnable onClick) {
        super(x, y, width, height);
        this.onClick = onClick;
    }

    @Override
    public final void click() {
        onClick.run();
    }

    @Override
    public final boolean isHovered(UIContext handler) {
        return x <= handler.mouseX() && handler.mouseX() <= x + width
                && y <= handler.mouseY() && handler.mouseY() <= y + height;
    }
}

package com.xenon.ui;

import com.xenon.ui.abstraction.AbstractButton;
import com.xenon.ui.abstraction.UIContext;

/**
 * Simple sample of what a TexturedButton class is going to look like. Usually, for widgets
 */
public class TexturedLabeledButton extends AbstractButton {

    protected final int tex_id;
    protected final String label;

    public TexturedLabeledButton(int texture_id, String label, int x, int y, int width, int height, Runnable onClick) {
        super(x, y, width, height, onClick);
        tex_id = texture_id;
        this.label = label;
    }

    @Override
    public void draw(UIContext handler) {
        //WorldRenderer.bindTextureLayer(tex_id);
    }
}

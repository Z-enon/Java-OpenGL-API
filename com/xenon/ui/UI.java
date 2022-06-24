package com.xenon.ui;

import com.xenon.ui.abstraction.AbstractButton;
import com.xenon.ui.abstraction.AbstractComponent;
import com.xenon.ui.abstraction.Drawable;
import com.xenon.ui.abstraction.UIContext;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Zenon
 */
public abstract class UI implements Drawable {

    private final List<AbstractComponent> components;
    private final List<AbstractButton> buttons;

    public UI(int drawableCap, int clickableCap) {
        components = new ArrayList<>(drawableCap);
        buttons = new ArrayList<>(clickableCap);
        init();
    }

    /**
     * Superclasses should register their components here.
     * @see #registerComponent(AbstractComponent)
     * @see #registerButton(AbstractButton)
     */
    protected abstract void init();

    /**
     * Adds a AbstractComponent to the render list. Meant to only be used inside {@link #init()}.
     * @param component the component to register
     */
    protected final void registerComponent(AbstractComponent component) {
        components.add(component);
    }
    /**
     * Adds a Button to the render list. Meant to only be used inside {@link #init()}.
     * @param button the button to register
     */
    protected final void registerButton(AbstractButton button) {
        buttons.add(button);
    }


    @Override
    public void draw(UIContext handler) {
        for (var d : components)
            d.draw(handler);
        for (var b : buttons)
            b.draw(handler);
    }

    /**
     * Calls {@link AbstractButton#click()} for every button that is hovered amongst {@link #buttons}.
     * @param handler the mouse coordinates wrapper
     */
    public void onClick(UIContext handler) {
        for (var b : buttons)
            if (b.isHovered(handler))
                b.click();
    }
}

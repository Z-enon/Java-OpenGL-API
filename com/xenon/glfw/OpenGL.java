package com.xenon.glfw;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a class or method requires OpenGL to be initialized.
 * More precisions can be given in <code>value</code>.
 * @author Zenon
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.TYPE})
public @interface OpenGL {
    String value();
}

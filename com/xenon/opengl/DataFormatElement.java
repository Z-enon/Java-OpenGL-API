package com.xenon.opengl;

import java.util.Arrays;

/**
 * @author Zenon
 */
public class DataFormatElement {

    public final int location;
    public final String GLSLType;

    public final String[] qualifiers;

    public final String name;

    public DataFormatElement(int loc, String glslType, String[] qualifiers, String name) {
        location = loc;
        GLSLType = glslType;
        this.qualifiers = qualifiers;
        this.name = name;
    }

    /**
     * Finds the first qualifier matching <code>toReplace</code> and replace it with <code>replaceWith</code>.
     * @param toReplace the qualifier to replace
     * @param replaceWith the qualifier to put instead
     */
    public void replaceQualifier(String toReplace, String replaceWith) {
        for (int i = 0; i < qualifiers.length; i++)
            if (qualifiers[i].equals(toReplace)) {
                qualifiers[i] = replaceWith;
                return;
            }
    }

    /**
     * Format the data for use in shaders, adding '\n' at the end.
     * @param appendTo the string builder
     */
    public void formatForShader(StringBuilder appendTo) {
        appendTo.append("layout (location=").append(location).append(')');
        for (String qualifier : qualifiers)
            appendTo.append(' ').append(qualifier);
        appendTo.append(' ').append(GLSLType).append(' ').append(name).append(';').append('\n');
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DataFormatElement format &&
                (format.location == location && format.GLSLType.equals(GLSLType) &&
                        Arrays.equals(format.qualifiers, qualifiers));
    }

    @Override
    public String toString() {
        return "DataFormatElement{" +
                "location=" + location +
                ", GLSLType='" + GLSLType + '\'' +
                ", qualifiers=" + Arrays.toString(qualifiers) +
                '}';
    }
}

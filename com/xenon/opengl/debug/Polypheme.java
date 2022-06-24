package com.xenon.opengl.debug;

import java.util.HashMap;
import java.util.Map;

/**
 * GLSL Library manager.
 * Statically-typed.
 * @author Zenon
 * @see Circe
 */
public class Polypheme {

    /**
     * The libraries accessed by their domains (#include <DOMAIN> in vertex shader code).
     */
    private static Map<String, String> codeByDomain = new HashMap<>();

    /**
     * Registers a library with the given domain, free of any blank space.
     * @param domain the domain used to access the library
     * @param code the library's code
     * @throws AssertionError if a library was already registered with such a domain
     */
    public static void registerLib(String domain, String code) {
        domain = domain.replaceAll("\\s", "");
        if (codeByDomain.containsKey(domain))
            throw new AssertionError(domain + " is already registered.");
        codeByDomain.put(domain, code);
    }

    /**
     *
     * @param domain the library's domain
     * @return the library corresponding to the given domain
     * @throws AssertionError if no library with such domain exists
     */
    public static String getLib(String domain) {
        String func = codeByDomain.get(domain);
        if (func == null)
            throw new AssertionError(domain+" isn't registered.");
        return func;
    }


    /**
     * Dispose of the libraries. Explicit nulling to help GC with big HashMap.
     */
    public static void dispose() {
        codeByDomain = null;
    }
}

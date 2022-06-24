package com.xenon.opengl.debug;

import com.xenon.opengl.DataFormatElement;
import com.xenon.opengl.VertexFormat;
import org.lwjgl.opengl.GLDebugMessageCallbackI;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.GL_INVALID_FRAMEBUFFER_OPERATION;

/**
 * Colossus debug/maintainability utils class.
 * Statically-typed.
 * @author Zenon
 */
public class Circe {


    public static String lookupBuffer(ByteBuffer buffer, int binding, VertexFormat format) {
        var b = new StringBuilder("------ ByteBuffer at ").append(buffer).append(" ------\n");
        List<VertexFormat.VertexFormatElement> els = new ArrayList<>();
        for (VertexFormat.VertexFormatElement el : format.elements()) {
            if (el.binding == binding)
                els.add(el);
        }
        for (int i=0; i < buffer.position();) {
            for (VertexFormat.VertexFormatElement el : els) {
                b.append("loc: ").append(el.location).append(" (");
                for (int __ = 0; __ < el.count; __++) {
                    switch (el.type) {
                        case GL_BYTE -> b.append(buffer.get(i));
                        case GL_UNSIGNED_BYTE -> b.append(Byte.toUnsignedInt(buffer.get(i)));
                        case GL_SHORT -> b.append(buffer.getShort(i));
                        case GL_UNSIGNED_SHORT -> b.append(Short.toUnsignedInt(buffer.getShort(i)));
                        case GL_FLOAT -> b.append(buffer.getFloat(i));
                        case GL_INT -> b.append(buffer.getInt(i));
                        case GL_UNSIGNED_INT -> b.append(Integer.toUnsignedLong(buffer.getInt(i)));
                        case GL_DOUBLE -> b.append(buffer.getDouble(i));
                        default -> throw new AssertionError("Unknown GL type");
                    }
                    b.append(',');
                    i += VertexFormat.sizeof(el.type);
                }
                b.deleteCharAt(b.length() - 1).append(')').append(';').append(' ');
            }
            b.append('\n');
        }
        return b.toString();
    }

    /**
     * Polls all the pending errors.
     * Now deprecated in favor of {@link org.lwjgl.opengl.GL46#glDebugMessageCallback(GLDebugMessageCallbackI, long)}.
     * @see DebugContext
     */
    @Deprecated
    public static void debugGLErrors(){
        for ( int error = glGetError(); error != GL_NO_ERROR; error = glGetError() ){
            System.out.println("GL Error : "+
                    switch(error){
                        case GL_INVALID_ENUM -> "Invalid Enum Type";
                        case GL_INVALID_VALUE -> "Invalid value";
                        case GL_INVALID_OPERATION -> "Invalid operation";
                        case GL_STACK_OVERFLOW -> "Pushing when stack full";
                        case GL_STACK_UNDERFLOW -> "Popping when stack empty";
                        case GL_OUT_OF_MEMORY -> "Out of Memory";
                        case GL_INVALID_FRAMEBUFFER_OPERATION -> "Invalid Framebuffer operation";
                        default -> "unknown error";
                    });
        }
        System.out.println("No error found");
    }

    /**
     * Checks whether all the vertex attributes of <code>format</code> are found in the supplied vertex shader's code.
     * Expecting no trailing or overriding vertex attrib in the given format.
     * @param code the vertex shader's code to be checked
     * @param format the VertexFormat to check with
     * @throws AssertionError if not all the vertex attributes in <code>format</code> are found in <code>code</code>.
     */
    public static void validateVertexShader(String code, VertexFormat format) {
        String[] lines = code.split("\n");
        VertexFormat.VertexFormatElement[] els = format.elements();
        boolean[] checked = new boolean[els.length];    // necessary in case VertexFormatElements are not in order
        for (var line : lines)
            if (line.contains("layout")) {
                line = line.replaceAll("\\s+", "");
                for (VertexFormat.VertexFormatElement el : els) {
                    int loc = el.location;
                    if (!checked[loc] && line.contains("location=" + loc) && line.contains("in"+el.GLSLType))
                        checked[loc] = true;
                }
            }

        for (int i=0; i < checked.length; i++)
            if (!checked[i]) throw new AssertionError("Detected incoherence for vertex attrib location "
                    + i + " in: "+code);
    }

    public static AbstractMap.SimpleEntry<String, String> parseVertexAndFragment(String code, VertexFormat format) {
        String[] lines = code.split("\n");
        Lexer lexer = Lexer.of(lines, 0, lines.length);
        skipEmptyLines(lexer);
        if (!lexer.hasNext() || !lexer.next().is("#") || !lexer.hasNext() || !lexer.next().is("vertex"))
            throw new AssertionError("Expected '#vertex' header at the beginning");
        checkHeaderEnd(lexer, true, "#vertex");

        var vertexBuilder = new StringBuilder();
        var fragmentBuilder = new StringBuilder();
        DataFormatElement[] vertexOutputs = parseShader(lexer, format.elements(), vertexBuilder);

        if (!lexer.hasNext() || !lexer.next().is("fragment"))
            throw new AssertionError("Expected '#fragment' to separate vertex code from fragment code");
        checkHeaderEnd(lexer, true, "#fragment");
        for (var vertexOutput : vertexOutputs)
            vertexOutput.replaceQualifier("out", "in");

        parseShader(lexer, vertexOutputs, fragmentBuilder);

        if (lexer.hasNext())
            throw new AssertionError("Fragment code ended with invalid header: "+lexer.next());
        return new AbstractMap.SimpleEntry<>(vertexBuilder.toString(), fragmentBuilder.toString());
    }

    public static AbstractMap.SimpleEntry<String, String> parseVertexAndFragmentSeparated(String vertexCode,
                                                                                          String fragmentCode,
                                                                                          VertexFormat format) {
        String[] vertexLines = vertexCode.split("\n");
        Lexer lexer = Lexer.of(vertexLines, 0, vertexLines.length);
        skipEmptyLines(lexer);
        if (!lexer.hasNext() || !lexer.next().is("#") || !lexer.hasNext() || !lexer.next().is("vertex"))
            throw new AssertionError("Expected '#vertex' header at the beginning");
        checkHeaderEnd(lexer, true, "#vertex");

        var vertexBuilder = new StringBuilder();
        var fragmentBuilder = new StringBuilder();
        DataFormatElement[] vertexOutputs = parseShader(lexer, format.elements(), vertexBuilder);

        if (lexer.hasNext())
            throw new AssertionError("Vertex code ended with invalid header: "+lexer.next());

        String[] fragLines = fragmentCode.split("\n");
        lexer = Lexer.of(fragLines, 0, fragLines.length);
        skipEmptyLines(lexer);
        if (!lexer.hasNext() || !lexer.next().is("#") || !lexer.hasNext() || !lexer.next().is("fragment"))
            throw new AssertionError("Expected '#fragment' header at the beginning");
        checkHeaderEnd(lexer, true, "#fragment");
        for (var vertexOutput : vertexOutputs)
            vertexOutput.replaceQualifier("out", "in");

        parseShader(lexer, vertexOutputs, fragmentBuilder);

        if (lexer.hasNext())
            throw new AssertionError("Fragment code ended with invalid header: "+lexer.next());
        return new AbstractMap.SimpleEntry<>(vertexBuilder.toString(), fragmentBuilder.toString());
    }

    /**
     * Main parsing function for GLSL shaders, be it vertex or fragment.
     * @param lexer the lexer
     * @param inputs the data format elements in input to validate
     * @param appendTo the string builder
     * @return the output formats
     */
    public static DataFormatElement[] parseShader(Lexer lexer, DataFormatElement[] inputs, StringBuilder appendTo) {
        skipEmptyLines(lexer);
        DataFormatElement[] outputs = null;
        boolean in_bracket = false, start_line = true;
        while(lexer.hasNext()) {
            Token t = lexer.next();
            final String s = t.content();
            switch (s) {
                case "#" -> {
                    if (!lexer.hasNext())
                        throw new AssertionError("Expected header keyword after '#', got nothing.");
                    start_line = true;
                    final String s1 = lexer.next().content;
                    switch (s1) {
                        case "inputs" -> {
                            for (DataFormatElement el : handleInputHeader(lexer, inputs))
                                el.formatForShader(appendTo);
                        }
                        case "outputs" -> {
                            outputs = handleOutputHeader(lexer);
                            for (DataFormatElement el : outputs)
                                el.formatForShader(appendTo);
                        }
                        case "include" -> handleLibHeader(lexer, appendTo);
                        case "if", "ifdef", "define", "undef", "ifndef", "else", "elif", "endif",
                                "error", "pragma", "extension", "line" -> {
                            start_line = false;
                            appendTo.append(s).append(' ').append(s1);
                        }
                        default -> {
                            lexer.pushLast();   // for higher order function to check if the header is valid
                            return outputs;
                        }
                    }
                }
                case "\n" -> {
                    start_line = true;
                    appendTo.append(s);
                }
                case ";", "," -> {
                    start_line = false;
                    appendTo.append(s);
                }
                case "{" -> {
                    start_line = false;
                    in_bracket = true;
                    appendTo.append(s);
                }
                case "}" -> {
                    start_line = false;
                    in_bracket = false;
                    appendTo.append(s);
                }
                default -> {
                    if (start_line) {
                        if (in_bracket)
                            appendTo.append(' ').append(' ').append(' ').append(' ');
                    } else appendTo.append(' ');
                    start_line = false;
                    appendTo.append(s);
                }
            }
        }
        if (in_bracket) // free syntax checking for compiler
            throw new AssertionError("Left bracket unclosed");

        return outputs;
    }

    /**
     * Correct syntax is:<br>
     * <code>#outputs &lt;location1 : type1 / name1; location2 : type2 / name2;...&gt;</code><br>
     * Resulting GLSL code is:
     * <code><pre>
     *     layout (location=location1) out type1 name1;
     *     layout (location=location2) out type2 name2;
     *     ...
     * </pre></code>
     * Additional qualifiers can be added:<br>
     * <code>#outputs &lt;location1[qualifier1, qualifier2] : type1 / name1&gt;</code><br>
     * Resulting GLSL code is:
     * <code><pre>
     *     layout (location=location1) qualifier1 qualifier2 out type1 name1;
     * </pre></code>
     * Using an empty array for qualifiers is invalid as is leaving a trailing comma.<br>
     * Note:<br>
     * The array of qualifiers is represented by a Set to prevent duplicates. That means if you manually write the 'out'
     * qualifier, its position won't be at the end! For example:
     * <code>[q1, q2]</code> and <code>[q1, q2, out]</code> become <code>q1 q2 out</code> while
     * <code>[q1, out, q2]</code> becomes <code>q1 out q2</code>, which is most likely invalid for the GLSL compiler.
     *
     * @param lexer the lexer
     * @return the resulting data formats
     * @see #handleDataFormatHeader(Lexer, String, String, Consumer)
     */
    private static DataFormatElement[] handleOutputHeader(Lexer lexer) {
        List<DataFormatElement> els = new ArrayList<>();
        handleDataFormatHeader(
                lexer,
                "#outputs <location[qualifiers...]: type / name; location: type / name;...>",
                "out",
                els::add
        );
        return els.toArray(new DataFormatElement[0]);
    }

    /**
     * Validates the '#inputs' header using the given DataFormatElement array.
     * Correct syntax is:<br>
     * <code>#inputs &lt;location1 : type1 / name1; location2 : type2 / name2;...&gt;</code><br>
     * Resulting GLSL code is:
     * <code><pre>
     *     layout (location=location1) in type1 name1;
     *     layout (location=location2) in type2 name2;
     *     ...
     * </pre></code>
     * Additional qualifiers can be added:<br>
     * <code>#inputs &lt;location1[qualifier1, qualifier2] : type1 / name1&gt;</code><br>
     * Resulting GLSL code is:
     * <code><pre>
     *     layout (location=location1) qualifier1 qualifier2 in type1 name1;
     * </pre></code>
     * Using an empty array for qualifiers is invalid as is leaving a trailing comma.<br>
     * Note:<br>
     * The array of qualifiers is represented by a Set to prevent duplicates. That means if you manually write the 'in'
     * qualifier, its position won't be at the end! For example:
     * <code>[q1, q2]</code> and <code>[q1, q2, in]</code> become <code>q1 q2 in</code> while
     * <code>[q1, in, q2]</code> becomes <code>q1 in q2</code>, which is most likely invalid for the GLSL compiler.
     *
     * @param lexer the lexer
     * @param els the expected formats as input. must be sorted by location ascending order
     * @return the parsed DataFormatElements
     * @see #handleDataFormatHeader(Lexer, String, String, Consumer)
     */
    private static DataFormatElement[] handleInputHeader(Lexer lexer, DataFormatElement[] els) {
        if (els == null || els.length == 0)
            throw new AssertionError("Inputs formats are null");
        final List<DataFormatElement> result = new ArrayList<>();
        handleDataFormatHeader(
                lexer,
                "#inputs <location[qualifiers...]: type / name; location: type / name;...>",
                "in",
                format -> {
                    if (format.location >= els.length || !els[format.location].equals(format))
                        throw new AssertionError("Parsed format doesn't match with expected format. " +
                                "Expected format: "+els[Math.max(0, Math.min(format.location, els.length - 1))] +
                                ". Parsed format: "+format);
                    result.add(format);
                });
        return result.toArray(new DataFormatElement[0]);
    }

    /**
     * Parses a format header, either '#inputs' or '#outputs', and call <code>consumer</code> for each data format
     * encountered in the header
     * @param lexer the lexer
     * @param syntax the correct syntax
     * @param defaultQualifier the default qualifier to be added at the end of the parsed qualifiers
     * @param consumer the consumer that is called for each parsed DataFormatElement
     * @throws AssertionError in case of parsing error
     * @see #handleInputHeader(Lexer, DataFormatElement[])
     * @see #handleOutputHeader(Lexer)
     */
    private static void handleDataFormatHeader(Lexer lexer, String syntax, String defaultQualifier,
                                               Consumer<DataFormatElement> consumer) {
        checkHeaderStart(lexer, syntax);
        boolean success = false;
        boolean expect_mark = false;    // whether we expect ':', '/' or ';' (or even '[' / ']', see below)
        /*
        special state where we declare the qualifiers (optional cz 'out' is automatically added)
         */
        boolean in_qualifiers = false;
        enum State {
            LOC(':'), TYPE('/'), NAME(';');

            final char expectAfter;
            State(char expectAfter) {
                this.expectAfter = expectAfter;
            }
            State cycle() {
                return switch(this) {
                    case LOC -> TYPE;
                    case TYPE -> NAME;
                    case NAME -> LOC;
                };
            }
        }
        State state = State.LOC;
        int currentLoc = -1;
        String currentType = null;
        Set<String> currentQualifiers = null;
        while (lexer.hasNext()) {
            Token t = lexer.next();
            if ((success = t.is(">")) || t.is("\n"))
                break;
            String s = t.content();

            if (expect_mark) {
                if ( !(s.length() == 1 && !Lexer.isWordChar(s.charAt(0))) )
                    throw new AssertionError("Expected either ':', '/' or ';'. Got '"+s+'\'');

                expect_mark = false;
                char c = s.charAt(0);

                if (state == State.LOC) {
                    if (in_qualifiers) {
                        if (c == ']') {
                            expect_mark = true;
                            in_qualifiers = false;
                        }
                        else if (c != ',')
                            throw new AssertionError("Qualifiers expected to be separated by commas.");
                        continue;
                    } else if (c == '[') {
                        currentQualifiers = new LinkedHashSet<>();
                        in_qualifiers = true;
                        continue;
                    }
                }

                if (c != state.expectAfter)
                    throw new AssertionError("Expected '" + state.expectAfter + "' after " + state +
                            ". Got '" + c + '\'');
                state = state.cycle();  // notice we cycle state here because we need the expectAfter validation

            } else {
                if ( s.length() == 1 && !Lexer.isWordChar(s.charAt(0)) )
                    throw new AssertionError("Expected "+state+". Got a mark: '"+s+'\'');
                switch(state) {
                    case LOC -> {
                        if (in_qualifiers) {
                            currentQualifiers.add(s);
                        } else {
                            try {
                                currentLoc = Integer.parseInt(s);
                            } catch(NumberFormatException e) {
                                throw new AssertionError("Expected an integer as location. Got '"+s+'\'');
                            }
                        }
                    }
                    case TYPE -> currentType = s;
                    case NAME -> {
                        String[] qualifiers;
                        if (currentQualifiers == null)
                            qualifiers = new String[] {defaultQualifier};
                        else {
                            currentQualifiers.add(defaultQualifier);
                            qualifiers = currentQualifiers.toArray(new String[0]);
                        }
                        consumer.accept(new DataFormatElement(
                                currentLoc,
                                currentType,
                                qualifiers, s
                        ));
                        currentQualifiers = null;
                    }
                }
                expect_mark = true;
            }
        }
        checkHeaderEnd(lexer, success && state == State.NAME, syntax);
    }


    /**
     * Appends the parsed library name to <code>appendTo</code>.
     * Correct header syntax is:<br>
     * <code>#include &lt;LIB_NAME&gt;</code>
     * where <code>LIB_NAME</code> is the library domain which must have been registered to {@link Polypheme}.
     * Note that the library domain can contain any non GLSL-word characters except '\n' and '>'.
     * @param lexer the lexer
     * @param appendTo the string builder
     * @throws AssertionError in case of parsing error or if <code>LIB_NAME</code> isn't registered in {@link Polypheme}
     */
    private static void handleLibHeader(Lexer lexer, StringBuilder appendTo) {
        checkHeaderStart(lexer, "#include <LIB_NAME>");
        var lib_name = new StringBuilder();
        boolean success = false;
        while (lexer.hasNext()) {
            String s = lexer.next().content();
            success = s.equals(">");
            if (success)
                break;
            lib_name.append(s);
        }
        checkHeaderEnd(lexer, success, "#include <LIB_NAME>");

        String lib_code = Polypheme.getLib(lib_name.toString());

        appendTo.append(lib_code).append('\n');
    }


    /**
     * Checks whether the header correctly starts with '<' for its parameter declaration.
     * @param lexer the lexer
     * @param syntax the correct syntax
     * @throws AssertionError if the next lexeme isn't '<'
     */
    private static void checkHeaderStart(Lexer lexer, String syntax) {
        if (!( lexer.hasNext() && lexer.next().is("<") ))
            throw new AssertionError("Expected '"+syntax+'\'');
    }

    /**
     * Checks if there's nothing trailing at the end.
     * @param lexer the lexer
     * @param encountered_gt whether we encountered '>' or we just ran out of lexemes
     * @param syntax the correct syntax
     * @throws AssertionError if encountered_gt is false or if the next lexeme isn't '\n'
     */
    private static void checkHeaderEnd(Lexer lexer, boolean encountered_gt, String syntax) {
        if (!encountered_gt || (lexer.hasNext() && !lexer.next().is("\n")))
            throw new AssertionError("Expected '"+syntax+"' without any anything trailing at the end");
    }

    /**
     * Skips all the empty lines.
     * Calls {@link Lexer#next()} until the lexeme isn't '\n' or there aren't any lexemes left
     * @param lexer the lexer
     */
    @SuppressWarnings("StatementWithEmptyBody")
    private static void skipEmptyLines(Lexer lexer) {
        boolean not_rewind = false;
        while(lexer.hasNext() && ( not_rewind = lexer.next().is("\n") ));
        if (!not_rewind)
            lexer.pushLast();
    }


    /**
     * Classic tokenizer for glsl syntax.
     * @author Zenon
     */
    public static class Lexer implements Iterator<Token> {

        private final Iterator<String> lineSupplier;
        /*
        'i' is the current index in the current line
         */
        private int i;
        private char[] currentLineChars;
        private boolean end_of_file;

        // whether we should reuse the current Token for the next next() call
        private boolean reuse;
        private Token current;

        /**
         *
         * @param shader the path to the shader
         * @return a new Lexer instance
         * @throws IOException if <code>Files.lines</code> fails
         */
        public static Lexer io(Path shader) throws IOException {
            try(Stream<String> lines = Files.lines(shader)) {
                return new Lexer(lines.iterator());
            }
        }
        /**
         *
         * @param lines the lines to parse
         * @param begin the beginning index in lines (inclusive)
         * @param end the end index in lines (exclusive)
         */
        public static Lexer of(String[] lines, int begin, int end) {
            if (lines.length < 1)
                throw new AssertionError("Supplied nothing to the parser");
            if (begin < 0 || end > lines.length)
                throw new AssertionError("Invalid bounds: begin=" + begin + ", end=" + end);
            return new Lexer(Arrays.stream(lines, begin, end).iterator());
        }

        private Lexer(Iterator<String> lineSupplier) {
            this.lineSupplier = lineSupplier;
            if (updateLine())
                throw new AssertionError("fed empty code to parser");
        }

        /**
         *
         * @return whether the next token is asserted not to be null
         */
        @Override
        public boolean hasNext() {
            return !end_of_file || reuse;
        }

        /**
         * Next {@link #next()} call should return the current value, and not update a new one.
         */
        public void pushLast() {
            reuse = true;
        }

        /**
         * @return the next Token available, null if none
         */
        @Override
        public Token next() {
            if (reuse) {
                reuse = false;
                return current;
            }
            return current = nextT();
        }

        /**
         * @return the next Token available, null if none
         */
        private Token nextT() {
            if (end_of_file)    throw new IllegalStateException("Lexer doesn't have any token left.");
            int len = currentLineChars.length;
            boolean in_word = false;
            int word_index_start = i;
            for (; i < len; i++) {
                char c = currentLineChars[i];
                if (isWordChar(c)) {
                    if (!in_word)
                        word_index_start = i;
                    in_word = true;
                } else {
                    if (in_word) return new Token(stringOf(currentLineChars, word_index_start, i));

                    if (c ==  '/')
                        if (i < len - 1 && currentLineChars[i + 1] == '/') break;

                    if (!Character.isWhitespace(c) || c == ';') {
                        i++;
                        return new Token(String.valueOf(c));
                    }
                }
            }

            if (in_word)
                return new Token(stringOf(currentLineChars, word_index_start, i));

            end_of_file = updateLine(); // correct because we call this iff the for loop broke
            return new Token("\n");
        }

        /**
         * Checks whether <code>c</code> is a word char in GLSL (i.e [a-zA-Z0-9_.]).
         * @param c the character to check
         * @return whether c is a letter or an underscore or a dot
         */
        public static boolean isWordChar(char c) {
            return c == '_' || c == '.' || ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || ('0' <= c && c <= '9');
        }

        /**
         * @return whether we reached the end of the lines
         */
        private boolean updateLine() {
            if (lineSupplier.hasNext()) {
                i = 0;
                currentLineChars = lineSupplier.next().toCharArray();
                return false;
            }
            return true;
        }

        /**
         * Equivalent to:
         * <code><pre>
         *     new String(chars, begin, end - begin);
         * </pre></code>
         */
        private static String stringOf(char[] chars, int begin, int end) {
            return new String(chars, begin, end - begin);
        }

    }

    /**
     * Simple token record for {@link Lexer}.
     * @param content the content of the token, be it a mark, a type or a name
     */
    public record Token(String content) {
        /**
         * @param s the content to test
         * @return whether {@link #content} is equal to <code>s</code>
         */
        public boolean is(String s) {
            return content.equals(s);
        }
    }

}

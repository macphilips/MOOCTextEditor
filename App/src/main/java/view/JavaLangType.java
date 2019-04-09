package view;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import org.eclipse.jdt.core.dom.*;
import org.fxmisc.richtext.StyleSpans;
import org.fxmisc.richtext.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaLangType implements LangType {
    private static final String[] KEYWORDS = new String[]{
            "abstract", "assert", "boolean", "break", "byte",
            "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else",
            "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import",
            "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super",
            "switch", "synchronized", "this", "throw", "throws",
            "transient", "try", "void", "volatile", "while"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";



    private class Visitor extends ASTVisitor {
        private final CompilationUnit cu;
        Set<String> names = new HashSet<>();

        Visitor(CompilationUnit cu) {
            this.cu = cu;
        }

        public boolean visit(VariableDeclarationFragment node) {
            SimpleName name = node.getName();
            this.names.add(name.getIdentifier());
            //System.out.println("Declaration of '" + name + "' at line" + cu.getLineNumber(name.getStartPosition()));
            return false; // do not continue to avoid usage info
        }

        public String getPattern() {
            String[] ss = new String[names.size()];
            int i = 0;
            for (String s : names) {
                ss[i++] = s;
            }
            return "\\b(" + String.join("|", ss) + ")\\b";
        }

        public boolean visit(SimpleName node) {
            if (this.names.contains(node.getIdentifier())) {
                // System.out.println("Usage of '" + node + "' at line " + cu.getLineNumber(node.getStartPosition()));
            }
            return true;
        }
    }

    public String extractFieldPattern(String text) {
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(text.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        Visitor visitor = new Visitor(cu);
        cu.accept(visitor);
        return visitor.getPattern();
    }

    public String format(String source) {
        try {
            return new Formatter().formatSource(source);
        } catch (FormatterException e) {
            e.printStackTrace();
        }
        return source;
    }

    @Override
    public StyleSpans<Collection<String>> computeHighlighting(String text) {
     //   System.out.println("Compute Thread => " + Thread.currentThread().getName());
        String fieldPattern = extractFieldPattern(text);
       // System.out.println(fieldPattern);
        Pattern PATTERN = Pattern.compile(
                "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                        + "|(?<PAREN>" + PAREN_PATTERN + ")"
                        + "|(?<BRACE>" + BRACE_PATTERN + ")"
                        + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                        + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                        + "|(?<STRING>" + STRING_PATTERN + ")"
                        + "|(?<FIELD>" + fieldPattern + ")"
                        + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
        );
        Matcher m = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> builder
                = new StyleSpansBuilder<>();
        while (m.find()) {
            String styleClass =
                    m.group("KEYWORD") != null ? "keyword" :
                            m.group("PAREN") != null ? "paren" :
                                    m.group("BRACE") != null ? "brace" :
                                            m.group("BRACKET") != null ? "bracket" :
                                                    m.group("SEMICOLON") != null ? "semicolon" :
                                                            m.group("STRING") != null ? "string" :
                                                                    m.group("COMMENT") != null ? "comment" :
                                                                            m.group("FIELD") != null ? "field" :
                                                                                    null; /* never happens */
            assert styleClass != null;
            builder.add(Collections.emptyList(), m.start() - lastKwEnd);
            builder.add(Collections.singleton(styleClass), m.end() - m.start());
            lastKwEnd = m.end();
        }
        builder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return builder.create();
    }
}

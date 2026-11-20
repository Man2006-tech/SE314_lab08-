package regex;

import java.util.ArrayList;
import java.util.List;

/**
 * Recursive-descent parser for the simplified html grammar:
 *   html   ::= ( normal | italic )*
 *   italic ::= '<i>' html '</i>'
 *   normal ::= text
 *   text   ::= [^<>]*
 * There is one private method for each nonterminal. Each method reads from
 * the current position in the string and advances that position.
 */
public class ItalicParser {

    private static final String OPEN  = "<i>";
    private static final String CLOSE = "</i>";

    /**
     * Parses a string into a list of html nodes.
     *
     * Precondition: s is not null.
     * Postcondition: returns the parse tree of s; empty text is not
     * represented by a node.
     *
     * @param s the string to parse
     * @return the top-level nodes of the parse tree
     * @throws IllegalArgumentException if s does not match the grammar
     */
    public static List<HtmlNode> parse(String s) {
        if (s == null) {
            throw new IllegalArgumentException("Input must not be null");
        }
        Parser parser = new Parser(s);
        List<HtmlNode> nodes = parser.html();
        if (!parser.atEnd()) {
            throw new IllegalArgumentException(
                    "Unexpected '" + parser.peek() + "' at position " + parser.position());
        }
        return nodes;
    }

    /**
     * Computes the deepest nesting of italic tags.
     *
     * Precondition: s matches the grammar.
     * Postcondition: returns 0 if there is no italic tag, otherwise the
     * maximum number of italic tags that enclose one another.
     *
     * @throws IllegalArgumentException if s does not match the grammar
     */
    public static int maxDepth(String s) {
        return depth(parse(s));
    }

    /**
     * Removes the tags from a string.
     *
     * Precondition: s matches the grammar.
     * Postcondition: returns the concatenation of all text in s, in order,
     * without any tag.
     *
     * @throws IllegalArgumentException if s does not match the grammar
     */
    public static String toPlainText(String s) {
        return plain(parse(s));
    }

    // Recursive helpers on the parse tree.
    private static int depth(List<HtmlNode> nodes) {
        int deepest = 0;                              // base case: no italic node
        for (HtmlNode node : nodes) {
            if (node instanceof Italic) {
                deepest = Math.max(deepest, 1 + depth(((Italic) node).getChildren()));
            }
        }
        return deepest;
    }

    private static String plain(List<HtmlNode> nodes) {
        StringBuilder sb = new StringBuilder();       // base case: empty list
        for (HtmlNode node : nodes) {
            if (node instanceof Text) {
                sb.append(((Text) node).getContent());
            } else {
                sb.append(plain(((Italic) node).getChildren()));
            }
        }
        return sb.toString();
    }

    /** Holds the string and the current position; one method per nonterminal. */
    private static class Parser {
        private final String s;
        private int pos = 0;

        Parser(String s) {
            this.s = s;
        }

        boolean atEnd()   { return pos >= s.length(); }
        int position()    { return pos; }
        char peek()       { return s.charAt(pos); }

        /** html ::= ( normal | italic )* */
        List<HtmlNode> html() {
            List<HtmlNode> nodes = new ArrayList<>();
            while (!atEnd() && !s.startsWith(CLOSE, pos)) {
                if (s.startsWith(OPEN, pos)) {
                    nodes.add(italic());
                } else {
                    nodes.add(normal());
                }
            }
            return nodes;
        }

        /** italic ::= '<i>' html '</i>' */
        HtmlNode italic() {
            expect(OPEN);
            List<HtmlNode> children = html();         // recursive step
            expect(CLOSE);
            return new Italic(children);
        }

        /** normal ::= text (a non-empty run, otherwise the input is malformed) */
        HtmlNode normal() {
            String content = text();
            if (content.isEmpty()) {
                throw new IllegalArgumentException(
                        "Unexpected '" + peek() + "' at position " + pos);
            }
            return new Text(content);
        }

        /** text ::= [^<>]* */
        String text() {
            int start = pos;
            while (!atEnd() && s.charAt(pos) != '<' && s.charAt(pos) != '>') {
                pos++;
            }
            return s.substring(start, pos);
        }

        private void expect(String token) {
            if (!s.startsWith(token, pos)) {
                throw new IllegalArgumentException(
                        "Expected " + token + " at position " + pos);
            }
            pos += token.length();
        }
    }
}

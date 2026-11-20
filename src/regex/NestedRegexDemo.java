package regex;

/**
 * Task 4, Part 4: tries to describe well-formed html with ONE regular
 * expression. A regex can only be built for a fixed maximum nesting depth.
 */
public class NestedRegexDemo {

    /** Builds a regex accepting html with italic nesting of at most k levels. */
    static String regexForDepth(int k) {
        String regex = "[^<>]*";                              // depth 0: plain text only
        for (int level = 1; level <= k; level++) {
            regex = "(?:[^<>]|<i>" + regex + "</i>)*";        // allow one more level
        }
        return regex;
    }

    static String nested(int depth) {
        StringBuilder sb = new StringBuilder("x");
        for (int i = 0; i < depth; i++) {
            sb.insert(0, "<i>").append("</i>");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        String regex2 = regexForDepth(2);
        System.out.println("Regex built for depth <= 2:");
        System.out.println(regex2);
        System.out.println();
        for (int depth = 0; depth <= 4; depth++) {
            String input = nested(depth);
            boolean byRegex = input.matches(regex2);
            boolean byParser;
            try {
                ItalicParser.parse(input);
                byParser = true;
            } catch (IllegalArgumentException e) {
                byParser = false;
            }
            System.out.printf("depth %d  %-28s regex: %-5b parser: %b%n",
                    depth, input, byRegex, byParser);
        }
    }
}

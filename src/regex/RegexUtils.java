package regex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods that validate and parse strings using regular expressions.
 * Every regular expression is assembled from small named pieces that mirror
 * the nonterminals of the grammars written in Task 2.
 *
 * Grammar for urls (Task 2, Part 1):
 *   url      ::= protocol '://' hostname (':' port)? path?
 *   protocol ::= 'http' | 'https' | 'ftp'
 *   hostname ::= (label '.')+ label
 *   label    ::= [a-zA-Z0-9-]+
 *   port     ::= [0-9]+
 *   path     ::= '/' | ('/' segment)+ '/'?
 *   segment  ::= [a-zA-Z0-9._~-]+
 *
 * Grammar for mobile numbers (Task 2, Part 2):
 *   mobile   ::= local | international
 *   local    ::= '03' digit digit '-' subscriber
 *   international ::= '+92' ' ' '3' digit digit ' ' subscriber
 *   subscriber ::= [0-9]{7}
 */
public class RegexUtils {

    // ---------------- named pieces of the url grammar ----------------
    private static final String PROTOCOL = "(https?|ftp)";
    private static final String LABEL    = "[a-zA-Z0-9-]+";
    private static final String HOSTNAME = "(" + LABEL + "\\.)+" + LABEL;
    private static final String PORT     = "[0-9]+";
    private static final String SEGMENT  = "[a-zA-Z0-9._~-]+";
    private static final String PATH     = "(/|(/" + SEGMENT + ")+/?)";
    private static final String URL_REGEX =
            PROTOCOL + "://" + HOSTNAME + "(:(" + PORT + "))?" + PATH + "?";

    // ---------------- named pieces of the mobile grammar ----------------
    private static final String SUBSCRIBER    = "[0-9]{7}";
    private static final String LOCAL         = "03[0-9]{2}-" + SUBSCRIBER;
    private static final String INTERNATIONAL = "\\+92 3[0-9]{2} " + SUBSCRIBER;
    private static final String MOBILE_REGEX  = "(" + LOCAL + "|" + INTERNATIONAL + ")";

    // ---------------- other named pieces ----------------
    private static final String COMMA_SEPARATOR = "\\s*,\\s*";
    private static final String USERNAME_CHAR   = "[A-Za-z0-9_-]";
    private static final int    MAX_PORT        = 65535;

    // ---------------- patterns, each compiled exactly once ----------------
    private static final Pattern PORT_IN_URL =
            Pattern.compile(PROTOCOL + "://" + HOSTNAME + ":(" + PORT + ")(/.*)?");
    private static final Pattern DATE =
            Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})");
    private static final Pattern MENTION =
            Pattern.compile("(?<!" + USERNAME_CHAR + ")@(" + USERNAME_CHAR + "+)");

    /**
     * Tests whether a string is a valid url.
     *
     * Precondition: s is not null.
     * Postcondition: returns true iff s matches the url grammar above and, if
     * a port is present, the port is an integer between 0 and 65535.
     * Returns false otherwise. Does not modify any state.
     *
     * @param s the string to test
     * @return true iff s is a valid url
     */
    public static boolean isValidUrl(String s) {
        if (!s.matches(URL_REGEX)) {
            return false;
        }
        Matcher m = PORT_IN_URL.matcher(s);
        if (m.matches()) {
            String digits = m.group(3).replaceFirst("^0+(?=.)", "");
            return digits.length() <= 5 && Integer.parseInt(digits) <= MAX_PORT;
        }
        return true;
    }

    /**
     * Tests whether a string is a Pakistani mobile number.
     *
     * Precondition: s is not null.
     * Postcondition: returns true iff s is written as 03XX-XXXXXXX or as
     * +92 3XX XXXXXXX (X is a digit). Returns false otherwise.
     *
     * @param s the string to test
     * @return true iff s is a valid mobile number
     */
    public static boolean isValidMobile(String s) {
        return s.matches(MOBILE_REGEX);
    }

    /**
     * Splits a comma-separated list.
     *
     * Precondition: s is not null.
     * Postcondition: returns the items of s in order, with the spaces around
     * each comma removed. An empty or blank string, and items that are empty
     * after trimming, produce no elements. The returned list is a new
     * modifiable list.
     *
     * @param s the comma-separated text
     * @return the list of trimmed, non-empty items
     */
    public static List<String> splitList(String s) {
        List<String> items = new ArrayList<>();
        if (s.trim().isEmpty()) {
            return items;
        }
        for (String part : s.trim().split(COMMA_SEPARATOR)) {
            if (!part.isEmpty()) {
                items.add(part);
            }
        }
        return items;
    }

    /**
     * Parses a date written as YYYY-MM-DD.
     *
     * Precondition: none (null is rejected with an exception).
     * Postcondition: returns {year, month, day} when s has the form
     * YYYY-MM-DD, 1 <= month <= 12 and 1 <= day <= 31.
     *
     * @param s the text to parse
     * @return an array {year, month, day}
     * @throws IllegalArgumentException if s is null, does not match the
     *         format, the month is not in 1..12 or the day is not in 1..31
     */
    public static int[] parseDate(String s) {
        if (s == null) {
            throw new IllegalArgumentException("Date must not be null");
        }
        Matcher m = DATE.matcher(s);
        if (!m.matches()) {
            throw new IllegalArgumentException("Date must have the form YYYY-MM-DD: " + s);
        }
        int year  = Integer.parseInt(m.group(1));
        int month = Integer.parseInt(m.group(2));
        int day   = Integer.parseInt(m.group(3));
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be 1 to 12: " + month);
        }
        if (day < 1 || day > 31) {
            throw new IllegalArgumentException("Day must be 1 to 31: " + day);
        }
        return new int[] { year, month, day };
    }

    /**
     * Finds the usernames mentioned in a text.
     *
     * Precondition: text is not null.
     * Postcondition: returns the set of usernames that appear as @username,
     * in lowercase, in order of first appearance. A username is one or more
     * letters, digits, underscores or hyphens. An @ that is immediately
     * preceded by a username character (as in bob@mit.edu) is not a mention.
     *
     * @param text the text to search
     * @return the lowercase usernames mentioned
     */
    public static Set<String> findMentions(String text) {
        Set<String> mentions = new LinkedHashSet<>();
        Matcher m = MENTION.matcher(text);
        while (m.find()) {
            mentions.add(m.group(1).toLowerCase());
        }
        return mentions;
    }

    /**
     * Tests whether a string is a valid YYYY-MM-DD date (see parseDate).
     *
     * @param s the string to test
     * @return true iff parseDate(s) succeeds
     */
    public static boolean isValidDate(String s) {
        try {
            parseDate(s);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Reads a text file and reports, for every line, whether it is a valid
     * url, mobile number or date.
     *
     * @param args args[0] is the path of the input file
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java regex.RegexUtils <input-file>");
            return;
        }
        try {
            for (String line : Files.readAllLines(Paths.get(args[0]))) {
                System.out.println("Line   : " + line);
                System.out.println("  URL    : " + isValidUrl(line));
                System.out.println("  Mobile : " + isValidMobile(line));
                System.out.println("  Date   : " + isValidDate(line));
            }
        } catch (IOException e) {
            System.out.println("Error: cannot read file: " + e.getMessage());
        }
    }
}

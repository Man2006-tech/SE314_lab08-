package regex;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class RegexUtilsTest {

    // ===================== isValidUrl =====================
    /*
     * Testing strategy for isValidUrl(s)
     * Partition on protocol:        http, https, ftp, unsupported (mailto, none)
     * Partition on hostname parts:  1 (invalid), 2, 3 or more
     * Partition on hostname chars:  letters only, contains digits, contains hyphen,
     *                               illegal character (underscore, space)
     * Partition on port:            absent, valid (0, 80, 65535), out of range (65536, 99999)
     * Partition on path:            absent, "/" only, one segment, several segments,
     *                               trailing slash, file name with a dot
     * Strings (a) to (f) of Task 1 are included as separate tests.
     */

    // Original lecture grammar (Task 1), reduced to a regex with the fix of Question 5.
    private static final String ORIGINAL_URL = "http://([a-z]+\\.)+[a-z]+(:[0-9]+)?/";
    private static final String BROKEN_URL   = "http://([a-z]+\\.)+[a-z]+(:[0-9]+)/";

    @Test
    void testTask1a() {
        assertTrue("http://mit.edu/".matches(ORIGINAL_URL));
        assertTrue(RegexUtils.isValidUrl("http://mit.edu/"));
    }

    @Test
    void testTask1b() {
        assertTrue("http://didit.csail.mit.edu:4949/".matches(ORIGINAL_URL));
        assertTrue(RegexUtils.isValidUrl("http://didit.csail.mit.edu:4949/"));
    }

    @Test
    void testTask1c() {
        assertFalse("http://ou812.com/".matches(ORIGINAL_URL));   // digits fail word
        assertTrue(RegexUtils.isValidUrl("http://ou812.com/"));    // allowed by extended grammar
    }

    @Test
    void testTask1d() {
        assertFalse("http://mit.edu".matches(ORIGINAL_URL));      // final '/' missing
        assertTrue(RegexUtils.isValidUrl("http://mit.edu"));       // path is optional now
    }

    @Test
    void testTask1e() {
        assertFalse("http://localhost/".matches(ORIGINAL_URL));   // hostname needs a dot
        assertFalse(RegexUtils.isValidUrl("http://localhost/"));
    }

    @Test
    void testTask1f() {
        assertFalse("https://mit.edu/".matches(ORIGINAL_URL));    // protocol is only http
        assertTrue(RegexUtils.isValidUrl("https://mit.edu/"));     // https now allowed
    }

    @Test
    void testBrokenRegexRequiresPort() {
        assertFalse("http://mit.edu/".matches(BROKEN_URL));       // the bug of Question 5
        assertTrue("http://mit.edu:80/".matches(BROKEN_URL));
    }

    @Test
    void testProtocols() {
        assertTrue(RegexUtils.isValidUrl("http://mit.edu"));
        assertTrue(RegexUtils.isValidUrl("https://mit.edu"));
        assertTrue(RegexUtils.isValidUrl("ftp://mit.edu"));
        assertFalse(RegexUtils.isValidUrl("mailto://mit.edu"));
        assertFalse(RegexUtils.isValidUrl("mit.edu"));
        assertFalse(RegexUtils.isValidUrl("://mit.edu"));
    }

    @Test
    void testHostnameParts() {
        assertFalse(RegexUtils.isValidUrl("http://mit/"));
        assertTrue(RegexUtils.isValidUrl("http://mit.edu/"));
        assertTrue(RegexUtils.isValidUrl("http://didit.csail.mit.edu/"));
        assertFalse(RegexUtils.isValidUrl("http://mit..edu/"));
        assertFalse(RegexUtils.isValidUrl("http://.mit.edu/"));
        assertFalse(RegexUtils.isValidUrl("http://mit.edu./"));
    }

    @Test
    void testHostnameCharacters() {
        assertTrue(RegexUtils.isValidUrl("http://my-site.edu.pk"));
        assertTrue(RegexUtils.isValidUrl("http://MIT.EDU"));
        assertFalse(RegexUtils.isValidUrl("http://my_site.edu"));
        assertFalse(RegexUtils.isValidUrl("http://my site.edu"));
    }

    @Test
    void testPortValid() {
        assertTrue(RegexUtils.isValidUrl("http://mit.edu:0/"));
        assertTrue(RegexUtils.isValidUrl("http://mit.edu:80"));
        assertTrue(RegexUtils.isValidUrl("http://mit.edu:65535/"));
        assertTrue(RegexUtils.isValidUrl("http://mit.edu:00080/"));
    }

    @Test
    void testPortOutOfRange() {
        assertFalse(RegexUtils.isValidUrl("http://mit.edu:65536/"));
        assertFalse(RegexUtils.isValidUrl("http://mit.edu:99999/"));
        assertFalse(RegexUtils.isValidUrl("http://mit.edu:123456789012/"));
    }

    @Test
    void testPortSyntaxErrors() {
        assertFalse(RegexUtils.isValidUrl("http://mit.edu:/"));
        assertFalse(RegexUtils.isValidUrl("http://mit.edu:ab/"));
    }

    @Test
    void testPaths() {
        assertTrue(RegexUtils.isValidUrl("http://mit.edu"));
        assertTrue(RegexUtils.isValidUrl("http://mit.edu/"));
        assertTrue(RegexUtils.isValidUrl("http://mit.edu/courses"));
        assertTrue(RegexUtils.isValidUrl("http://mit.edu/courses/se314/"));
        assertTrue(RegexUtils.isValidUrl("https://mit.edu:8080/courses/se314/lab09.html"));
        assertFalse(RegexUtils.isValidUrl("http://mit.edu//courses"));
        assertFalse(RegexUtils.isValidUrl("http://mit.edu/a b"));
    }

    @Test
    void testUrlEmptyString() {
        assertFalse(RegexUtils.isValidUrl(""));
    }

    // ===================== isValidMobile =====================
    /*
     * Testing strategy for isValidMobile(s)
     * Partition on form:            local (03XX-XXXXXXX), international (+92 3XX XXXXXXX), other
     * Partition on the digit after 0 / +92:  3, not 3
     * Partition on digit count:     correct, too few, too many
     * Partition on separators:      correct, missing, wrong (space for dash, dash for space)
     */
    @Test
    void testMobileLocal() {
        assertTrue(RegexUtils.isValidMobile("0333-1234567"));
        assertTrue(RegexUtils.isValidMobile("0300-0000000"));
    }

    @Test
    void testMobileInternational() {
        assertTrue(RegexUtils.isValidMobile("+92 333 1234567"));
        assertTrue(RegexUtils.isValidMobile("+92 300 7654321"));
    }

    @Test
    void testMobileWrongLeadingDigit() {
        assertFalse(RegexUtils.isValidMobile("0433-1234567"));
        assertFalse(RegexUtils.isValidMobile("+92 433 1234567"));
    }

    @Test
    void testMobileWrongLength() {
        assertFalse(RegexUtils.isValidMobile("0333-123456"));
        assertFalse(RegexUtils.isValidMobile("0333-12345678"));
        assertFalse(RegexUtils.isValidMobile("+92 333 123456"));
    }

    @Test
    void testMobileWrongSeparators() {
        assertFalse(RegexUtils.isValidMobile("03331234567"));
        assertFalse(RegexUtils.isValidMobile("0333 1234567"));
        assertFalse(RegexUtils.isValidMobile("+92-333-1234567"));
        assertFalse(RegexUtils.isValidMobile("+923331234567"));
        assertFalse(RegexUtils.isValidMobile("92 333 1234567"));
    }

    @Test
    void testMobileOtherInputs() {
        assertFalse(RegexUtils.isValidMobile(""));
        assertFalse(RegexUtils.isValidMobile("abcd-efghijk"));
        assertFalse(RegexUtils.isValidMobile(" 0333-1234567"));
    }

    // ===================== splitList =====================
    /*
     * Testing strategy for splitList(s)
     * Partition on s:               empty, blank, one item, several items
     * Partition on spaces:          none, before commas, after commas, around the whole string
     * Partition on items:           all non-empty, contains an empty item (",,")
     */
    @Test
    void testSplitListSpaces() {
        assertEquals(Arrays.asList("apple", "banana", "cherry"),
                RegexUtils.splitList("apple ,banana,  cherry"));
    }

    @Test
    void testSplitListNoSpaces() {
        assertEquals(Arrays.asList("a", "b", "c"), RegexUtils.splitList("a,b,c"));
    }

    @Test
    void testSplitListSingleItem() {
        assertEquals(Collections.singletonList("apple"), RegexUtils.splitList("apple"));
    }

    @Test
    void testSplitListEmptyAndBlank() {
        assertTrue(RegexUtils.splitList("").isEmpty());
        assertTrue(RegexUtils.splitList("   ").isEmpty());
    }

    @Test
    void testSplitListSurroundingSpaces() {
        assertEquals(Arrays.asList("x", "y"), RegexUtils.splitList("   x , y   "));
    }

    @Test
    void testSplitListInternalSpacesKept() {
        assertEquals(Arrays.asList("new york", "lahore"),
                RegexUtils.splitList("new york , lahore"));
    }

    @Test
    void testSplitListEmptyItemsDropped() {
        assertEquals(Arrays.asList("a", "b"), RegexUtils.splitList("a,,b,"));
    }

    // ===================== parseDate =====================
    /*
     * Testing strategy for parseDate(s)
     * Partition on format:          correct, wrong separator, wrong digit counts, letters, null, empty
     * Partition on month:           1, 2..11, 12, 0, 13
     * Partition on day:             1, 2..30, 31, 0, 32
     */
    @Test
    void testParseDateTypical() {
        assertArrayEquals(new int[] { 2026, 11, 20 }, RegexUtils.parseDate("2026-11-20"));
    }

    @Test
    void testParseDateBoundaries() {
        assertArrayEquals(new int[] { 2000, 1, 1 }, RegexUtils.parseDate("2000-01-01"));
        assertArrayEquals(new int[] { 1999, 12, 31 }, RegexUtils.parseDate("1999-12-31"));
    }

    @Test
    void testParseDateBadFormat() {
        assertThrows(IllegalArgumentException.class, () -> RegexUtils.parseDate("2026/11/20"));
        assertThrows(IllegalArgumentException.class, () -> RegexUtils.parseDate("2026-1-20"));
        assertThrows(IllegalArgumentException.class, () -> RegexUtils.parseDate("26-11-20"));
        assertThrows(IllegalArgumentException.class, () -> RegexUtils.parseDate("2026-11-20x"));
        assertThrows(IllegalArgumentException.class, () -> RegexUtils.parseDate("abcd-ef-gh"));
        assertThrows(IllegalArgumentException.class, () -> RegexUtils.parseDate(""));
        assertThrows(IllegalArgumentException.class, () -> RegexUtils.parseDate(null));
    }

    @Test
    void testParseDateBadMonth() {
        assertThrows(IllegalArgumentException.class, () -> RegexUtils.parseDate("2026-00-10"));
        assertThrows(IllegalArgumentException.class, () -> RegexUtils.parseDate("2026-13-10"));
    }

    @Test
    void testParseDateBadDay() {
        assertThrows(IllegalArgumentException.class, () -> RegexUtils.parseDate("2026-05-00"));
        assertThrows(IllegalArgumentException.class, () -> RegexUtils.parseDate("2026-05-32"));
    }

    // ===================== findMentions =====================
    /*
     * Testing strategy for findMentions(text)
     * Partition on number of mentions:   0, 1, many
     * Partition on repeats:              all different, same user twice, same user with different case
     * Partition on username characters:  letters, digits, underscore, hyphen
     * Partition on the character before @: start of text, space, punctuation, username character
     * Partition on text:                 empty, "@" alone
     */
    @Test
    void testMentionsNone() {
        assertTrue(RegexUtils.findMentions("no mentions here").isEmpty());
        assertTrue(RegexUtils.findMentions("").isEmpty());
        assertTrue(RegexUtils.findMentions("@").isEmpty());
    }

    @Test
    void testMentionsSingleAndMany() {
        assertEquals(set("alice"), RegexUtils.findMentions("hi @Alice!"));
        assertEquals(set("alice", "bob"), RegexUtils.findMentions("@alice and @bob"));
    }

    @Test
    void testMentionsCaseAndRepeats() {
        assertEquals(set("alice"), RegexUtils.findMentions("@Alice @ALICE @alice"));
    }

    @Test
    void testMentionsUsernameCharacters() {
        assertEquals(set("user_1", "a-b"), RegexUtils.findMentions("@user_1, @a-b."));
    }

    @Test
    void testMentionsAtStartAndAfterPunctuation() {
        assertEquals(set("a", "b"), RegexUtils.findMentions("@a,(@b)"));
    }

    @Test
    void testMentionsEmailIsNotMention() {
        assertTrue(RegexUtils.findMentions("bob@mit.edu").isEmpty());
        assertEquals(set("carol"), RegexUtils.findMentions("mail bob@mit.edu or ask @carol"));
    }

    private static Set<String> set(String... names) {
        return new LinkedHashSet<>(Arrays.asList(names));
    }
}

package regex;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class ItalicParserTest {

    // ===================== parse =====================
    /*
     * Testing strategy for parse(s)
     * Partition on s:        empty, plain text only, italic only, text before/after italic,
     *                        several siblings, nested (depth 2 and 3), empty italic
     * Partition on validity: well-formed, unclosed <i>, stray </i>, unsupported tag,
     *                        stray '<' or '>', null
     * Strings of Task 1, Question 6 are included.
     */
    @Test
    void testParseEmpty() {
        assertEquals(Collections.emptyList(), ItalicParser.parse(""));
    }

    @Test
    void testParsePlainText() {
        assertEquals(Arrays.<HtmlNode>asList(new Text("hello")), ItalicParser.parse("hello"));
    }

    @Test
    void testParseTask1Example() {                       // a<i>b</i>c
        List<HtmlNode> expected = Arrays.asList(
                new Text("a"), new Italic(Arrays.<HtmlNode>asList(new Text("b"))), new Text("c"));
        assertEquals(expected, ItalicParser.parse("a<i>b</i>c"));
    }

    @Test
    void testParseNestedItalics() {                      // <i><i>x</i></i>
        HtmlNode inner = new Italic(Arrays.<HtmlNode>asList(new Text("x")));
        HtmlNode outer = new Italic(Arrays.asList(inner));
        assertEquals(Arrays.asList(outer), ItalicParser.parse("<i><i>x</i></i>"));
    }

    @Test
    void testParseSiblings() {
        List<HtmlNode> expected = Arrays.asList(
                new Italic(Arrays.<HtmlNode>asList(new Text("a"))),
                new Italic(Arrays.<HtmlNode>asList(new Text("b"))));
        assertEquals(expected, ItalicParser.parse("<i>a</i><i>b</i>"));
    }

    @Test
    void testParseEmptyItalic() {
        assertEquals(Arrays.<HtmlNode>asList(new Italic(Collections.<HtmlNode>emptyList())),
                ItalicParser.parse("<i></i>"));
    }

    @Test
    void testParseUnclosedItalic() {                     // <i>x
        assertThrows(IllegalArgumentException.class, () -> ItalicParser.parse("<i>x"));
    }

    @Test
    void testParseStrayClosingTag() {
        assertThrows(IllegalArgumentException.class, () -> ItalicParser.parse("x</i>"));
        assertThrows(IllegalArgumentException.class, () -> ItalicParser.parse("<i>x</i></i>"));
    }

    @Test
    void testParseUnsupportedTag() {                     // a<b>c
        assertThrows(IllegalArgumentException.class, () -> ItalicParser.parse("a<b>c"));
    }

    @Test
    void testParseStrayAngleBrackets() {
        assertThrows(IllegalArgumentException.class, () -> ItalicParser.parse("a<b"));
        assertThrows(IllegalArgumentException.class, () -> ItalicParser.parse("a>b"));
        assertThrows(IllegalArgumentException.class, () -> ItalicParser.parse("<i>x</i"));
    }

    @Test
    void testParseNull() {
        assertThrows(IllegalArgumentException.class, () -> ItalicParser.parse(null));
    }

    // ===================== maxDepth =====================
    /*
     * Testing strategy for maxDepth(s)
     * Partition on depth:      0 (empty or plain), 1, 2, 3
     * Partition on structure:  one chain, siblings of different depth, malformed input
     */
    @Test
    void testMaxDepthZero() {
        assertEquals(0, ItalicParser.maxDepth(""));
        assertEquals(0, ItalicParser.maxDepth("plain"));
    }

    @Test
    void testMaxDepthOne() {
        assertEquals(1, ItalicParser.maxDepth("a<i>b</i>c"));
    }

    @Test
    void testMaxDepthTwo() {
        assertEquals(2, ItalicParser.maxDepth("a<i>b<i>c</i></i>"));
        assertEquals(2, ItalicParser.maxDepth("<i><i>x</i></i>"));
    }

    @Test
    void testMaxDepthThree() {
        assertEquals(3, ItalicParser.maxDepth("<i><i><i>x</i></i></i>"));
    }

    @Test
    void testMaxDepthSiblings() {
        assertEquals(2, ItalicParser.maxDepth("<i>a</i><i>b<i>c</i></i><i>d</i>"));
    }

    @Test
    void testMaxDepthMalformed() {
        assertThrows(IllegalArgumentException.class, () -> ItalicParser.maxDepth("<i>x"));
    }

    // ===================== toPlainText =====================
    /*
     * Testing strategy for toPlainText(s)
     * Partition on s:   empty, no tags, one tag, nested tags, siblings, empty italic, malformed
     */
    @Test
    void testPlainTextEmptyAndNoTags() {
        assertEquals("", ItalicParser.toPlainText(""));
        assertEquals("plain", ItalicParser.toPlainText("plain"));
    }

    @Test
    void testPlainTextOneTag() {
        assertEquals("abc", ItalicParser.toPlainText("a<i>b</i>c"));
    }

    @Test
    void testPlainTextNested() {
        assertEquals("abc", ItalicParser.toPlainText("a<i>b<i>c</i></i>"));
        assertEquals("x", ItalicParser.toPlainText("<i><i>x</i></i>"));
    }

    @Test
    void testPlainTextSiblingsAndEmptyItalic() {
        assertEquals("ab", ItalicParser.toPlainText("<i>a</i><i></i><i>b</i>"));
    }

    @Test
    void testPlainTextKeepsSpaces() {
        assertEquals("one two three", ItalicParser.toPlainText("one <i>two</i> three"));
    }

    @Test
    void testPlainTextMalformed() {
        assertThrows(IllegalArgumentException.class, () -> ItalicParser.toPlainText("a<b>c"));
    }
}

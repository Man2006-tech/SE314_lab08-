package regex;

import java.util.Objects;

/** A run of plain text containing no '<' or '>' characters. Immutable. */
public final class Text implements HtmlNode {
    private final String content;

    /** @param content the text; must not be null */
    public Text(String content) {
        this.content = Objects.requireNonNull(content);
    }

    /** @return the text */
    public String getContent() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Text && ((Text) o).content.equals(content);
    }

    @Override
    public int hashCode() {
        return content.hashCode();
    }

    @Override
    public String toString() {
        return "Text(\"" + content + "\")";
    }
}

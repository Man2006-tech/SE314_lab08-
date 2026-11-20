package regex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Italic text: any number of child nodes between <i> and </i>. Immutable. */
public final class Italic implements HtmlNode {
    private final List<HtmlNode> children;

    /** @param children the nodes inside the italic tag; must not be null */
    public Italic(List<HtmlNode> children) {
        this.children = Collections.unmodifiableList(new ArrayList<>(children));
    }

    /** @return an unmodifiable list of the child nodes */
    public List<HtmlNode> getChildren() {
        return children;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Italic && ((Italic) o).children.equals(children);
    }

    @Override
    public int hashCode() {
        return children.hashCode();
    }

    @Override
    public String toString() {
        return "Italic" + children;
    }
}

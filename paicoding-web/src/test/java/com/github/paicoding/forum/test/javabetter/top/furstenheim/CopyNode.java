package com.github.paicoding.forum.test.javabetter.top.furstenheim;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

class CopyNode {
    private static final String[] VOID_ELEMENTS = {
            "area", "base", "br", "col", "command", "embed", "hr", "img", "input",
            "keygen", "link", "meta", "param", "source", "track", "wbr"
    };
    private static final String[] MEANINGFUL_WHEN_BLANK_ELEMENTS = {
            "a", "table", "thead", "tbody", "tfoot", "th", "td", "iframe", "script",
            "audio", "video"
            };

    private static final String[] BLOCK_ELEMENTS = {
            "address", "article", "aside", "audio", "blockquote", "body", "canvas",
            "center", "dd", "dir", "div", "dl", "dt", "fieldset", "figcaption", "figure",
            "footer", "form", "frameset", "h1", "h2", "h3", "h4", "h5", "h6", "header",
            "hgroup", "hr", "html", "isindex", "li", "main", "menu", "nav", "noframes",
            "noscript", "ol", "output", "p", "pre", "section", "table", "tbody", "td",
            "tfoot", "th", "thead", "tr", "ul"
            };

    private static Set<String> VOID_ELEMENTS_SET = null;
    private static Set<String> MEANINGFUL_WHEN_BLANK_ELEMENTS_SET = null;
    private static Set<String> BLOCK_ELEMENTS_SET = null;

    Node element;
    CopyNode parent;

    CopyNode (String input) {
        Document document = Jsoup.parse(
                // DOM parsers arrange elements in the <head> and <body>.
                // Wrapping in a custom element ensures elements are reliably arranged in
                // a single element.
                "<x-copydown id=\"copydown-root\">" + input + "</x-copydown>");
        Element root = document.getElementById("copydown-root");
        new WhitespaceCollapser().collapse(root);
        element = root;
    }

    CopyNode (Node node, CopyNode parent) {
        element = node;
        this.parent = parent;
    }

    boolean isCode () {
        // TODO cache in property to avoid escalating to root
        return element.nodeName().equals("code") || (parent != null && parent.isCode());
    }

    static boolean isBlank (Node element) {
        String textContent;
        if (element instanceof Element) {
            textContent = ((Element)element).wholeText();
        } else {
            textContent = element.outerHtml();
        }
        return !isVoid(element) &&
               !isMeaningfulWhenBlank(element) &&
               // TODO check text is the same as textContent in browser
               Pattern.compile("(?i)^\\s*$").matcher(textContent).find() &&
               !hasVoidNodesSet(element) &&
               !hasMeaningfulWhenBlankNodesSet(element);
    }
    FlankingWhiteSpaces flankingWhitespace () {
        String leading = "";
        String trailing = "";
        if (!isBlock(element)) {
            String textContent;
            if (element instanceof Element) {
                textContent = ((Element)element).wholeText();
            } else {
                textContent = element.outerHtml();
            }
            // TODO original uses textContent
            boolean hasLeading = Pattern.compile("^\\s").matcher(textContent).find();
            boolean hasTrailing = Pattern.compile("\\s$").matcher(textContent).find();
            // TODO maybe make node property and avoid recomputing
            boolean blankWithSpaces = isBlank(element) && hasLeading && hasTrailing;
            if (hasLeading && !isLeftFlankedByWhitespaces()) {
                leading = " ";
            }
            if (!blankWithSpaces && hasTrailing && !isRightFlankedByWhitespaces()) {
                trailing = " ";
            }
        }
        return new FlankingWhiteSpaces(leading, trailing);
    }

    private boolean isLeftFlankedByWhitespaces () {
        return isChildFlankedByWhitespaces(" $", element.previousSibling());
    }
    private boolean isRightFlankedByWhitespaces () {
        return isChildFlankedByWhitespaces("^ ", element.nextSibling());
    }
    private boolean isChildFlankedByWhitespaces (String regex, Node sibling) {
        if (sibling == null) {
            return false;
        }
        if (NodeUtils.isNodeType3(sibling)) {
            // TODO fix. Originally sibling.nodeValue
            return Pattern.compile(regex).matcher(sibling.outerHtml()).find();
        }
        if (NodeUtils.isNodeType1(sibling)) {
            // TODO fix. Originally textContent
            return Pattern.compile(regex).matcher(sibling.outerHtml()).find();
        }
        return false;
    }

    private static boolean hasVoidNodesSet (Node node) {
        if (!(node instanceof Element)) {
            return false;
        }
        Element element = (Element) node;

        for (String tagName: VOID_ELEMENTS_SET) {
            if (element.getElementsByTag(tagName).size() != 0) {
                return true;
            }
        }
        return false;
    }
    static boolean isVoid (Node element) {
        return getVoidNodesSet().contains(element.nodeName());
    }
    private static Set<String> getVoidNodesSet() {
        if (VOID_ELEMENTS_SET != null) {
            return VOID_ELEMENTS_SET;
        }
        VOID_ELEMENTS_SET = new HashSet<>(Arrays.asList(VOID_ELEMENTS));
        return VOID_ELEMENTS_SET;
    }

    private static boolean hasMeaningfulWhenBlankNodesSet (Node node) {
        if (!(node instanceof Element)) {
            return false;
        }
        Element element = (Element) node;
        for (String tagName: MEANINGFUL_WHEN_BLANK_ELEMENTS_SET) {
            if (element.getElementsByTag(tagName).size() != 0) {
                return true;
            }
        }
        return false;
    }
    private static boolean isMeaningfulWhenBlank (Node element) {
        return getMeaningfulWhenBlankNodesSet().contains(element.nodeName());
    }
    private static Set<String> getMeaningfulWhenBlankNodesSet() {
        if (MEANINGFUL_WHEN_BLANK_ELEMENTS_SET != null) {
            return MEANINGFUL_WHEN_BLANK_ELEMENTS_SET;
        }
        MEANINGFUL_WHEN_BLANK_ELEMENTS_SET = new HashSet<>(Arrays.asList(MEANINGFUL_WHEN_BLANK_ELEMENTS));
        return MEANINGFUL_WHEN_BLANK_ELEMENTS_SET;
    }

    private boolean hasBlockNodesSet (Node node) {
        if (!(node instanceof Element)) {
            return false;
        }
        Element element = (Element) node;
        for (String tagName: BLOCK_ELEMENTS_SET) {
            if (element.getElementsByTag(tagName).size() != 0) {
                return true;
            }
        }
        return false;
    }
    static boolean isBlock (Node element) {
        return getBlockNodesSet().contains(element.nodeName());
    }

    private static Set<String> getBlockNodesSet() {
        if (BLOCK_ELEMENTS_SET != null) {
            return BLOCK_ELEMENTS_SET;
        }
        BLOCK_ELEMENTS_SET = new HashSet<>(Arrays.asList(BLOCK_ELEMENTS));
        return BLOCK_ELEMENTS_SET;
    }

    static class FlankingWhiteSpaces {
        String getLeading() {
            return leading;
        }

        String getTrailing() {
            return trailing;
        }

        private final String leading;
        private final String trailing;

        FlankingWhiteSpaces(String leading, String trailing) {
            this.leading = leading;
            this.trailing = trailing;
        }
    }
}

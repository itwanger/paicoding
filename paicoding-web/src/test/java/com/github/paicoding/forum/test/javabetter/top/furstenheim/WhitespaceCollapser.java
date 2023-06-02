package com.github.paicoding.forum.test.javabetter.top.furstenheim;

import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.regex.Pattern;

/**
 * The Whitespace collapser is originally adapted from collapse-whitespace
 * by Luc Thevenard.
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Luc Thevenard <lucthevenard@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

class WhitespaceCollapser {
    /**
     * Remove extraneous whitespace from the given element. Modifies the node in place
     * @param element
     */
    void collapse (Node element) {
        if (element.childNodeSize() == 0 || isPre(element)) {
            return;
        }

        TextNode prevText = null;
        boolean prevVoid = false;
        Node prev = null;
        Node node = next(prev, element);

        // Traverse the tree
        while (node != element) {
            if (NodeUtils.isNodeType3(node) || NodeUtils.isNodeType4(node)) {
                TextNode textNode = (TextNode) node;
                String value = textNode.attributes().get("#text").replaceAll("[ \\r\\n\\t]+", " ");
                if ((prevText == null || Pattern.compile(" $").matcher(prevText.text()).find()) &&!prevVoid && value.charAt(0) == ' ') {
            value = value.substring(1);
                }
                if (value.length() == 0) {
                    node = remove(node);
                    continue;
                }
                TextNode newNode = new TextNode(value);
                node.replaceWith(newNode);
                prevText = newNode;
                node = newNode;
            } else if (NodeUtils.isNodeType1(node)) {
                if (isBlock(node)) {
                    if (prevText != null) {
                        prevText.text(prevText.text().replaceAll(" $",""));
                    }
                    prevText = null;
                    prevVoid = false;
                } else if (isVoid(node)) {
                    // avoid trimming space around non block, non br void elements
                    prevText = null;
                    prevVoid = true;
                }
            } else {
                node = remove(node);
                continue;
            }
            Node nextNode = next(prev, node);
            prev = node;
            node = nextNode;
        }
        if (prevText != null) {
            prevText.text(prevText.text().replaceAll(" $", ""));
            if (prevText.text() == null) {
                remove(prevText);
            }
        }

    }

    /**
     * remove(node) removes the given node from the DOM and returns the
     * next node in the sequence.
     *
     * @param {Node} node
     * @return {Node} node
     */
    private Node remove (Node node) {
        Node next = node.nextSibling() != null ? node.nextSibling() : (Node)node.parentNode();
        node.remove();
        return next;
    }
    /**
     * Returns next node in the sequence given current and previous nodes
     */
    private Node next (Node prev, Node current) {
        if ((prev != null && prev.parent() == current) || isPre(current)) {
            // TODO beware parentNode might not be element
            return current.nextSibling() != null ? current.nextSibling() : current.parentNode();
        }
        if (current.childNodeSize() != 0) {
            return current.childNode(0);
        }
        if (current.nextSibling() != null) {
            return current.nextSibling();
        }
        return (Node)current.parentNode();
    }
    private boolean isPre (Node element) {
        // TODO allow to override with lambda in options
        return element.nodeName().equals("pre");
    }

    private boolean isBlock (Node element) {
        // TODO allow to override with lambda in optiosn
        return CopyNode.isBlock(element) ||  element.nodeName().equals("br");
    }

    private boolean isVoid (Node element) {
        // Allow to override
        return CopyNode.isVoid(element);
    }

}

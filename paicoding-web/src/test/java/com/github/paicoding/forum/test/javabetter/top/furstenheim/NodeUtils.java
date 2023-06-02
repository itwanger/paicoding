package com.github.paicoding.forum.test.javabetter.top.furstenheim;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

// Methods missing from jsoup
class NodeUtils {
    static boolean isNodeType1 (Node element) {
        return element instanceof Element;
    }
    static boolean isNodeType3 (Node element) {
        return element.nodeName().equals("text") || element.nodeName().equals("#text");
    }
    // CDATA section node
    static boolean isNodeType4 (Node element) {
        return false;
    }
}

package com.github.paicoding.forum.test.javabetter.top.furstenheim;

import org.jsoup.nodes.Node;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

class Rule {
    private Predicate<Node> filter;
    private BiFunction<String, Node, String> replacement;

    public Supplier<String> getAppend() {
        return append;
    }

    private Supplier<String> append = null;

    public void setName(String name) {
        this.name = name;
    }

    private String name;

    Rule (String filter, BiFunction<String, Node, String> replacement) {
        this.filter = (el) -> el.nodeName().toLowerCase() == filter;
        this.replacement = replacement;
    }

    Rule (String[] filters, BiFunction<String, Node, String> replacement) {
        Set<String> availableFilters = new HashSet<String>(Arrays.asList(filters));
        filter = (element -> availableFilters.contains(element.nodeName()));
        this.replacement = replacement;
    }
    Rule(Predicate<Node> filter, BiFunction<String, Node, String> replacement) {
        this.filter = filter;
        this.replacement = replacement;
    }
    Rule(Predicate<Node> filter, BiFunction<String, Node, String> replacement, Supplier<String> append) {
        this.filter = filter;
        this.replacement = replacement;
        this.append = append;
    }

    Predicate<Node> getFilter() {
        return filter;
    }

    BiFunction<String, Node, String> getReplacement() {
        return replacement;
    }
}

package com.sstewartgallus.frontend;

import java.util.List;
import java.util.stream.Collectors;

public interface Node {
    static Atom of(String value) {
        return new Atom(value);
    }

    static Array of(List<Node> nodes) {
        return new Array(nodes);
    }

    record Array(List<Node>nodes) implements Node {
        public String toString() {
            return "(" + nodes.stream().map(Object::toString).collect(Collectors.joining(" ")) + ")";
        }
    }

    record Atom(String value) implements Node {
        public String toString() {
            return value;
        }
    }
}

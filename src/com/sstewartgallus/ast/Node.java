package com.sstewartgallus.ast;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface Node {
    static Atom of(String value) {
        return new Atom(value);
    }

    static Array of(List<Node> nodes) {
        return new Array(nodes);
    }

    record Array(List<Node>nodes) implements Node {

        public <T> T parse(Function<String, T> onString, Function<List<T>, T> onNodes) {
            return onNodes.apply(parseHelper(onString, onNodes));
        }

        <T> List<T> parseHelper(Function<String, T> onString, Function<List<T>, T> onNodes) {
            return nodes.stream().map(node -> {
                if (node instanceof Atom str) {
                    return onString.apply(str.value());
                }
                if (node instanceof Array l) {
                    return onNodes.apply(l.parseHelper(onString, onNodes));
                }
                throw new IllegalStateException("unreachable " + node.getClass());
            }).collect(Collectors.toUnmodifiableList());
        }
    }

    record Atom(String value) implements Node {
        public String toString() {
            return value;
        }
    }
}

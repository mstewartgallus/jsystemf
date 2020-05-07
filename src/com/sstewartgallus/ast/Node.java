package com.sstewartgallus.ast;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.IntFunction;

public interface Node {
    static Atom of(String value) {
        return new Atom(value);
    }

    static Array of(Node... nodes) {
        return new Array(nodes);
    }

    record Array(Node... nodes) implements Node {

        public String toString() {
            return Arrays.toString(nodes);
        }

        public <T> T parse(Function<String, T> onString, Function<T[], T> onNodes, IntFunction<T[]> mkArray) {
            return onNodes.apply(parseHelper(onString, onNodes, mkArray));
        }

        <T> T[] parseHelper(Function<String, T> onString, Function<T[], T> onNodes, IntFunction<T[]> mkArray) {
            return Arrays.stream(nodes).map(node -> {
                if (node instanceof Atom str) {
                    return onString.apply(str.value());
                }
                if (node instanceof Array l) {
                    return onNodes.apply(l.parseHelper(onString, onNodes, mkArray));
                }
                throw new IllegalStateException("unreachable " + node.getClass());
            }).toArray(mkArray);
        }
    }

    record Atom(String value) implements Node {
        public String toString() {
            return value;
        }
    }
}

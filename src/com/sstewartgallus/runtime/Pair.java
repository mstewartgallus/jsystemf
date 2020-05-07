package com.sstewartgallus.runtime;

// fixme.. Interface?
// defer to beans linker for now
public record Pair<A, B>(A first, B second)/* extends Value<T<A, B>> */ {

    public static <A, B> Pair<A, B> of(A first, B second) {
        return new Pair<>(first, second);
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }
}

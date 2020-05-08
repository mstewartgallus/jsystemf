package com.sstewartgallus.runtime;

// fixme... reify hlist..
public record ConsValue(Object head, ConsValue tail)/* extends Value<T<A, B>> */ {

    public static ConsValue of(Object head, ConsValue tail) {
        return new ConsValue(head, tail);
    }

    public Object getHead() {
        return head;
    }

    public ConsValue getTail() {
        return tail;
    }

    public String toString() {
        return "(" + head + ":" + tail + ")";
    }
}

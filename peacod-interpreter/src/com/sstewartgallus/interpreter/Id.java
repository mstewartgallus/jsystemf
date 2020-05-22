package com.sstewartgallus.interpreter;

public class Id<A> implements Comparable<Id<?>> {
    @Override
    public int compareTo(Id<?> o) {
        return hashCode() - o.hashCode();
    }
}

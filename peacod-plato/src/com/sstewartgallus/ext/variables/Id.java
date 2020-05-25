package com.sstewartgallus.ext.variables;

public final class Id<A> implements Comparable<Id<?>> {
    @Override
    public int compareTo(Id<?> o) {
        return hashCode() - o.hashCode();
    }

    @Override
    public String toString() {
        return "Id@" + hashCode();
    }
}

package com.sstewartgallus.ext.variables;

public final class Id<A> implements Comparable<Id<?>> {
    public String toString() {
        return String.valueOf(hashCode());
    }

    @Override
    public int compareTo(Id<?> var) {
        return var.hashCode() - hashCode();
    }
}

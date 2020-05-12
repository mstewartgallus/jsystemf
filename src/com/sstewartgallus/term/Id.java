package com.sstewartgallus.term;

public record Id<A>(int number) implements Comparable<Id<?>> {
    public String toString() {
        return "v" + number();
    }

    @Override
    public int compareTo(Id<?> var) {
        return var.number() - number();
    }
}

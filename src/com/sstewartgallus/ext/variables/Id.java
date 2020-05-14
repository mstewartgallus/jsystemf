package com.sstewartgallus.ext.variables;

public record Id<A>(int number) implements Comparable<Id<?>> {
    public String toString() {
        return String.valueOf(number);
    }

    @Override
    public int compareTo(Id<?> var) {
        return var.number - number;
    }
}

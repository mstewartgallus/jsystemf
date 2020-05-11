package com.sstewartgallus.type;

public record Var<A>(int number) implements Comparable<Var<?>> {
    public String toString() {
        return "v" + number();
    }

    @Override
    public int compareTo(Var<?> var) {
        return var.number() - number();
    }
}

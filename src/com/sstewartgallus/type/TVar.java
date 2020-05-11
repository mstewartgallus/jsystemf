package com.sstewartgallus.type;

public record TVar<A>(int number) implements Comparable<TVar<?>> {
    public String toString() {
        return "t" + number();
    }

    @Override
    public int compareTo(TVar<?> var) {
        return var.number() - number();
    }
}

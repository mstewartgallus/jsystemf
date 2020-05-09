package com.sstewartgallus.term;

import com.sstewartgallus.type.Type;

public record Var<A>(Type<A>type, int number) implements Comparable<Var<?>> {
    public String toString() {
        return "v" + number();
    }

    @Override
    public int compareTo(com.sstewartgallus.term.Var<?> var) {
        return var.number() - number();
    }
}

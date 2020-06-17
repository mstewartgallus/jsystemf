package com.sstewartgallus.plato.ir.systemf;

import com.sstewartgallus.plato.ir.type.TypeDesc;

import java.util.concurrent.atomic.AtomicLong;

public record Variable<A>(TypeDesc<A>type, String name) implements Comparable<Variable<?>> {
    private static final AtomicLong IDS = new AtomicLong(0);

    public static <A> Variable<A> newInstance(TypeDesc<A> t) {
        return new Variable<>(t, "v_" + IDS.getAndAdd(1));
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Variable<?> o) {
        return toString().compareTo(o.toString());
    }
}

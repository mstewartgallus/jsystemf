package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.type.TypeDesc;

import java.util.concurrent.atomic.AtomicLong;

public record Lbl<A>(TypeDesc<A>type, String name) implements Comparable<Lbl<?>> {
    private static final AtomicLong IDS = new AtomicLong(0);

    public static <A> Lbl<A> newInstance(TypeDesc<A> t) {
        return new Lbl<>(t, "l_" + IDS.getAndAdd(1));
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Lbl<?> o) {
        return toString().compareTo(o.toString());
    }
}

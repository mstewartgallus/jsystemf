package com.sstewartgallus.plato.ir;

import com.sstewartgallus.plato.ir.type.TypeDesc;

import java.util.concurrent.atomic.AtomicLong;

public record Label<A>(TypeDesc<A>type, String name) implements Comparable<Label<?>> {
    private static final AtomicLong IDS = new AtomicLong(0);

    public static <A> Label<A> newInstance(TypeDesc<A> t) {
        return new Label<>(t, "l_" + IDS.getAndAdd(1));
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Label<?> o) {
        return toString().compareTo(o.toString());
    }
}

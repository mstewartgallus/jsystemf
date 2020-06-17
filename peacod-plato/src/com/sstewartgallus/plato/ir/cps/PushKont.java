package com.sstewartgallus.plato.ir.cps;


import com.sstewartgallus.plato.runtime.Fn;

public record PushKont<A, B>(Value<A>head, Kont<B>tail) implements Kont<Fn<A, B>> {
    @Override
    public String toString() {
        return "(" + head + " :: " + tail + ")";
    }
}


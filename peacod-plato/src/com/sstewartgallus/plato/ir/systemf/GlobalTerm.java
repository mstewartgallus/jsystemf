package com.sstewartgallus.plato.ir.systemf;

import com.sstewartgallus.plato.ir.type.TypeDesc;

public record GlobalTerm<A>(Global<A>global) implements Term<A> {
    @Override
    public String toString() {
        return global.toString();
    }

    @Override
    public TypeDesc<A> type() {
        return global.type();
    }
}

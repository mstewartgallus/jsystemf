package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.Jit;

public interface Literal<A> {
    TypeDesc<A> type();

    default void compile(Jit.Environment environment) {
        throw new UnsupportedOperationException(getClass().toString());
    }
}


package com.sstewartgallus.plato.ir.systemf;

import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.Fn;
import com.sstewartgallus.plato.runtime.U;

import java.util.Objects;

public record ApplyTerm<A, B>(Term<Fn<U<A>, B>>f,
                              Term<A>x) implements Term<B> {
    public ApplyTerm {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    @Override
    public TypeDesc<B> type() {
        var fType = (TypeDesc.TypeApplicationDesc<B, Fn<U<A>, B>>) f.type();
        return fType.x();
    }

    @Override
    public String toString() {
        return "(" + f + " " + x + ")";
    }
}

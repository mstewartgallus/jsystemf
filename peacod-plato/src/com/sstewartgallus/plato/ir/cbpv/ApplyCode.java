package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.Fn;
import com.sstewartgallus.plato.runtime.Jit;

import java.util.Objects;

public record ApplyCode<A, B>(Code<Fn<A, B>>f, Literal<A>x) implements Code<B> {
    public ApplyCode {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    @Override
    public void compile(Jit.Environment environment) {
        x.compile(environment);
        f.compile(environment);
    }

    @Override
    public TypeDesc<B> type() {
        var fType = (TypeDesc.TypeApplicationDesc<B, Fn<A, B>>) f.type();
        return fType.x();
    }

    @Override
    public String toString() {
        return x + "\n" + f;
    }
}

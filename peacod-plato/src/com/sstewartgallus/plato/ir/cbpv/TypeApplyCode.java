package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.type.Type;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.V;

import java.util.Objects;

public record TypeApplyCode<A, B>(Code<V<A, B>>f, Type<A>x) implements Code<B> {
    public TypeApplyCode {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    public static <A, B> TypeApplyCode<A, B> of(Code<V<A, B>> f, Type<A> x) {
        return new TypeApplyCode<>(f, x);
    }

    @Override
    public String toString() {
        return x + "\n" + f;
    }

    @Override
    public TypeDesc<B> type() {
        return null;
    }
}

package com.sstewartgallus.plato.cbpv;

import com.sstewartgallus.plato.runtime.V;
import com.sstewartgallus.plato.syntax.type.ForallType;
import com.sstewartgallus.plato.syntax.type.Type;
import com.sstewartgallus.plato.syntax.type.TypeCheckException;

import java.util.Objects;

public record TypeApplyCode<A, B>(Code<V<A, B>>f, Type<A>x) implements Code<B> {
    public TypeApplyCode {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    @Override
    public Type<B> type() throws TypeCheckException {
        return ((ForallType<A, B>) f.type()).f().apply(x);
    }
}

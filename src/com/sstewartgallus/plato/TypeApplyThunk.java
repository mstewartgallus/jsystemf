package com.sstewartgallus.plato;

import java.util.Objects;

public record TypeApplyThunk<A, B>(Term<V<A, B>>f, Type<A>x) implements ThunkTerm<B> {
    public TypeApplyThunk {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    @Override
    public Type<B> type() throws TypeCheckException {
        return ((Type.Forall<A, B>) f.type()).f().apply(x);
    }

    @Override
    public String toString() {
        return "{" + f + " " + x + "}";
    }
}

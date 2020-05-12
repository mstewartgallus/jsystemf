package com.sstewartgallus.plato;

import java.util.Objects;

public record TypeApplyThunk<A, B>(Term<V<A, B>>f, Type<A>x) implements ThunkTerm<B>, CoreTerm<B> {
    public TypeApplyThunk {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    @Override
    public Type<B> type() throws TypeCheckException {
        return ((ForallNormal<A, B>) f.type()).f().apply(x);
    }

    @Override
    public String toString() {
        return "{" + f + " " + x + "}";
    }

    @Override
    public Term<B> stepThunk() {
        // fixme... do types need to be normalized as well?
        // fixme... type check?
        var fNorm = (TypeLambdaValue<A, B>) Interpreter.normalize(f);
        // fixme... should I normalize the argument?
        return fNorm.f().apply(x);
    }
}

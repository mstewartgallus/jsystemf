package com.sstewartgallus.plato;

import java.util.Objects;

public record ApplyThunk<A, B>(Term<F<A, B>>f, Term<A>x) implements ThunkTerm<B>, CoreTerm<B> {
    public ApplyThunk {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    @Override
    public Type<B> type() throws TypeCheckException {
        var fType = f.type();

        var funType = (FunctionNormal<A, B>) fType;
        var range = funType.range();

        var argType = x.type();

        fType.unify(argType.to(range));

        return funType.range();
    }

    @Override
    public String toString() {
        return "(" + f + " " + x + ")";
    }

    @Override
    public Term<B> stepThunk() {
        var fNorm = (FunctionValue<A, B>) Interpreter.normalize(f);
        return fNorm.apply(x);
    }
}

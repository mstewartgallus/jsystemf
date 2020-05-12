package com.sstewartgallus.pass1;

import com.sstewartgallus.plato.*;

import java.util.Objects;
import java.util.function.Function;

public record TupleApplyThunk<A extends HList<A>, B>(Term<F<A, B>> f, Term<A> x) implements ThunkTerm<B> {
    public TupleApplyThunk {
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
    public Term<B> stepThunk() {
        var fNorm = (LambdaValue<A, B>) Interpreter.normalize(f);
        return fNorm.apply(x);
    }
}

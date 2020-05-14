package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.plato.*;

import java.util.Objects;

public record TupleApplyThunk<A, B>(Term<A>f, Arg<A, B>x) implements ThunkTerm<B> {
    public TupleApplyThunk {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    private static <A, B, C> Term<C> applyParam(LambdaValue<A, B> f, Arg.Add<A, B, C> x) {
        var result = f.apply(x.argument());
        return new TupleApplyThunk<>(result, x.tail());
    }

    @Override
    public Type<B> type() throws TypeCheckException {
        // fixme...
        throw null;
    }

    @Override
    public Term<B> stepThunk() {
        return x.apply(f);
    }
}

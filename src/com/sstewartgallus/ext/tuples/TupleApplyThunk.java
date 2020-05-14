package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.ThunkTerm;
import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.TypeCheckException;

import java.util.Objects;

public record TupleApplyThunk<A, B>(Term<A>f, Arg<A, B>x) implements ThunkTerm<B> {
    public TupleApplyThunk {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    @Override
    public Term<B> visitChildren(Visitor visitor) {
        throw null;
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

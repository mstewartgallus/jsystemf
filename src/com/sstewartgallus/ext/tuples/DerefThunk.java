package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.plato.*;

import java.util.Objects;

public record DerefThunk<B extends HList<B>, A>(Term<Cons<A, B>>product) implements ThunkTerm<A> {
    public DerefThunk {
        Objects.requireNonNull(product);
    }

    @Override
    public Type<A> type() throws TypeCheckException {
        return ((ConsType<A, B>) product.type()).head();
    }

    @Override
    public Term<A> stepThunk() {
        var productNorm = (ConsValue<A, B>) Interpreter.normalize(product);
        return productNorm.head();
    }

    @Override
    public String toString() {
        return "(get " + product + ")";
    }
}

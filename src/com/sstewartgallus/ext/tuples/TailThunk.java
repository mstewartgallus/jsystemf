package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.plato.*;

import java.util.Objects;

// fixme... extract to interface....
public record TailThunk<A, B extends HList<B>>(Term<HList.Cons<A, B>>product) implements ThunkTerm<B>, Getter<B> {
    public TailThunk {
        Objects.requireNonNull(product);
    }

    @Override
    public Type<B> type() throws TypeCheckException {
        return ((ConsNormal<A, B>) product.type()).tail();
    }

    @Override
    public Term<B> stepThunk() {
        var productNorm = (ConsValue<A, B>) Interpreter.normalize(product);
        return productNorm.tail();
    }

    @Override
    public String toString() {
        return "(tail " + product + ")";
    }
}

package com.sstewartgallus.pass1;

import com.sstewartgallus.plato.*;

import java.util.Objects;

public record TailThunk<A, B extends HList<B>>(Term<HList.Cons<A, B>>product) implements ThunkTerm<B> {
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

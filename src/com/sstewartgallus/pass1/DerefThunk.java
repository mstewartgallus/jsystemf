package com.sstewartgallus.pass1;

import com.sstewartgallus.plato.*;

import java.util.Objects;

public record DerefThunk<A, B extends HList<B>>(Term<HList.Cons<A, B>>product) implements ThunkTerm<A> {
    public DerefThunk {
        Objects.requireNonNull(product);
    }

    @Override
    public Type<A> type() throws TypeCheckException {
        return ((ConsNormal<A, B>) product.type()).head();
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

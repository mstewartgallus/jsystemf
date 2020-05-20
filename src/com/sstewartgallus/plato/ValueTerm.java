package com.sstewartgallus.plato;

import java.util.function.Function;

// fixme... make abstract base class?
public interface ValueTerm<A> extends Term<A> {
    default <B> Term<B> stepThunk(Function<ValueTerm<A>, Term<B>> k) {
        return k.apply(this);
    }
}

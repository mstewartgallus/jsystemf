package com.sstewartgallus.plato;

public interface LambdaValue<A, B> extends ValueTerm<F<A, B>> {
    Term<B> apply(Term<A> x);
}

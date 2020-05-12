package com.sstewartgallus.plato;

public interface FunctionValue<A, B> extends ValueTerm<F<A, B>> {
    Term<B> apply(Term<A> x);
}

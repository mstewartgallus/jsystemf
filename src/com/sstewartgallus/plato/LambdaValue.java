package com.sstewartgallus.plato;

import java.util.Objects;

public abstract class LambdaValue<A, B> implements ValueTerm<F<A, B>>, LambdaTerm<F<A, B>> {
    private final Type<A> domain;

    public LambdaValue(Type<A> domain) {
        Objects.requireNonNull(domain);
        this.domain = domain;
    }

    public Type<A> domain() {
        return domain;
    }

    public abstract Term<B> apply(Term<A> x);
}

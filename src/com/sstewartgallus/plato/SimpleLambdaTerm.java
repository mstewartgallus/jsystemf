package com.sstewartgallus.plato;

import java.util.Objects;
import java.util.function.Function;

final class SimpleLambdaTerm<A, B> extends LambdaTerm<A, B> implements ValueTerm<F<A, B>>, Term<F<A, B>> {
    private final Function<Term<A>, Term<B>> f;

    public SimpleLambdaTerm(Type<A> domain, Type<B> range,
                            Function<Term<A>, Term<B>> f) {
        super(domain, range);
        Objects.requireNonNull(f);
        this.f = f;
    }

    public Term<B> apply(Term<A> x) {
        return f.apply(x);
    }
}

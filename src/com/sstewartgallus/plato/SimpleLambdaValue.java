package com.sstewartgallus.plato;

import java.util.Objects;
import java.util.function.Function;

public final class SimpleLambdaValue<A, B> extends LambdaValue<A, B> implements ValueTerm<F<A, B>>, LambdaTerm<F<A, B>> {
    private final Function<Term<A>, Term<B>> f;

    public SimpleLambdaValue(Type<A> domain,
                             Function<Term<A>, Term<B>> f) {
        super(domain);
        Objects.requireNonNull(f);
        this.f = f;
    }

    public Term<B> apply(Term<A> x) {
        return f.apply(x);
    }
}

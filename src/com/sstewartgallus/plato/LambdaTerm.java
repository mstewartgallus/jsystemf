package com.sstewartgallus.plato;

import java.util.Objects;


public abstract class LambdaTerm<A, B> implements ValueTerm<F<A, B>> {
    private final Type<A> domain;
    private final Type<B> range;

    public LambdaTerm(Type<A> domain, Type<B> range) {
        Objects.requireNonNull(domain);
        this.domain = domain;
        this.range = range;
    }

    public final Type<A> domain() {
        return domain;
    }

    // perhaps use a kontinuation like thunks?
    public abstract Term<B> apply(Term<A> x);

    @Override
    public final Term<F<A, B>> visitChildren(Visitor visitor) {
        var v = new VarTerm<>(domain());
        var body = visitor.term(apply(v));
        return new LambdaTerm<>(visitor.type(domain), visitor.type(range)) {
            @Override
            public Term<B> apply(Term<A> x) {
                return v.substituteIn(body, x);
            }
        };
    }

    @Override
    public final Type<F<A, B>> type() throws TypeCheckException {
        return domain.to(range);
    }

    public Type<B> range() {
        return range;
    }
}

package com.sstewartgallus.plato;

import java.util.Objects;
import java.util.function.Function;

public final class SimpleTypeLambdaValue<A, B> extends TypeLambdaValue<A, B> {
    private final Function<Type<A>, Term<B>> f;

    public SimpleTypeLambdaValue(Function<Type<A>, Term<B>> f) {
        Objects.requireNonNull(f);
        this.f = f;
    }

    @Override
    public Term<V<A, B>> visitChildren(Visitor visitor) {
        return new SimpleTypeLambdaValue<>(x -> visitor.term(f.apply(x)));
    }

    public Term<B> apply(Type<A> x) {
        return this.f.apply(x);
    }
}

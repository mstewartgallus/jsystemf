package com.sstewartgallus.plato;

import java.util.function.Function;

/**
 * This is intended to be pristine source language untainted by compiler stuff.
 * <p>
 * Any processing should happen AFTER this step.
 */
public interface Type<X> {
    Type<Integer> INT = new PureNormal<>(int.class);

    static <A, B> Type<V<A, B>> v(Function<Type<A>, Type<B>> f) {
        return new ForallNormal<>(f);
    }

    // fixme... rethink unification...
    <Y> Type<X> unify(Type<Y> right) throws TypeCheckException;

    default <B> Term<F<X, B>> l(Function<Term<X>, Term<B>> f) {
        return new LambdaValue<>(this, f);
    }

    default <B> Type<F<X, B>> to(Type<B> range) {
        return new FunctionNormal<>(this, range);
    }

    <T> Type<X> substitute(Id<T> v, Type<T> replacement);
}

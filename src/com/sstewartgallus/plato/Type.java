package com.sstewartgallus.plato;

import com.sstewartgallus.ext.java.J;
import com.sstewartgallus.ext.java.JavaType;

import java.util.List;
import java.util.function.Function;

/**
 * This is intended to be pristine source language untainted by compiler stuff.
 * <p>
 * Any processing should happen AFTER this step.
 */
public interface Type<X> {
    Type<J<Integer>> INT = new JavaType<>(int.class);

    static <A, B> Type<V<A, B>> v(Function<Type<A>, Type<B>> f) {
        return new ForallType<>(f);
    }

    // fixme... rethink unification...
    <Y> Type<X> unify(Type<Y> right) throws TypeCheckException;

    default <B> Term<F<X, B>> l(Function<Term<X>, Term<B>> f) {
        return new LambdaValue<>(this, f);
    }

    default <B> Type<F<X, B>> to(Type<B> range) {
        return new FunctionType<>(this, range);
    }

    default Type<X> visitChildren(Term.Visitor visitor) {
        throw null;
    }

    default Type<X> visit(Term.Visitor visitor) {
        return visitor.type(this);
    }

    default Class<?> erase() {
        throw new UnsupportedOperationException(getClass().toString());
    }

    default List<Class<?>> flatten() {
        return List.of(erase());
    }
}

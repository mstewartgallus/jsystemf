package com.sstewartgallus.plato;

import com.sstewartgallus.ext.java.J;
import com.sstewartgallus.ext.java.JavaType;
import com.sstewartgallus.ext.pretty.PrettyThunk;
import com.sstewartgallus.runtime.TypeDesc;

import java.lang.constant.Constable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * This is intended to be pristine source language untainted by compiler stuff.
 * <p>
 * Any processing should happen AFTER this step.
 */
public interface Type<X> extends Constable {
    Type<J<Integer>> INT = new JavaType<>(int.class);

    static <A, B> Type<V<A, B>> v(Function<Type<A>, Type<B>> f) {
        return new ForallType<>(f);
    }

    default Optional<TypeDesc<X>> describeConstable() {
        throw new UnsupportedOperationException(getClass().toString());
    }

    // fixme... rethink unification...
    <Y> Type<X> unify(Type<Y> right) throws TypeCheckException;

    default <B> ValueTerm<F<X, B>> l(Function<Term<X>, Term<B>> f) {
        try (var pretty = PrettyThunk.generate(this)) {
            var range = f.apply(pretty).type();
            return new SimpleLambdaValue<>(this, range, f);
        }
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

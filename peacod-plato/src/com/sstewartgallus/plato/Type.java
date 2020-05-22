package com.sstewartgallus.plato;

import com.sstewartgallus.ext.pretty.PrettyTag;

import java.lang.constant.Constable;
import java.util.Optional;
import java.util.function.Function;

/**
 * This is intended to be pristine source language untainted by compiler stuff.
 * <p>
 * Any processing should happen AFTER this step.
 */
public interface Type<X> extends Constable {

    static <A, B> Type<V<A, B>> v(Function<Type<A>, Type<B>> f) {
        return new ForallType<>(f);
    }

    static <A, B> Type<B> apply(Type<V<A, B>> f, Type<A> x) {
        if (f instanceof ForallType<A, B> forall) {
            return forall.f().apply(x);
        }
        return new TypeApplyType<>(f, x);
    }

    // fixme... how to move out...
    default Optional<TypeDesc<X>> describeConstable() {
        throw new UnsupportedOperationException(getClass().toString());
    }

    // fixme... rethink unification...
    <Y> Type<X> unify(Type<Y> right) throws TypeCheckException;

    default <B> ValueTerm<F<X, B>> l(Function<Term<X>, Term<B>> f) {
        // fixme... how to fix type inference...
        try (var pretty = PrettyTag.<X>generate()) {
            var range = f.apply(NominalTerm.ofTag(pretty, this)).type();
            return new SimpleLambdaTerm<>(this, range, f);
        }
    }

    default <B> Type<F<X, B>> to(Type<B> range) {
        return new TypeApplyType<>(new TypeApplyType<>(Helper.function(), this), range);
    }

    default Type<X> visitChildren(Term.Visitor visitor) {
        throw null;
    }

    default Type<X> visit(Term.Visitor visitor) {
        return visitor.type(this);
    }

    // fixme.. how to move out... ?
    default Class<?> erase() {
        throw new UnsupportedOperationException(getClass().toString());
    }
}

class Helper {
    static final NominalType FUNCTION = NominalType.ofTag(FunctionTag.function());

    static <A, B> Type<V<A, V<B, F<A, B>>>> function() {
        return FUNCTION;
    }
}

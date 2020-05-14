package com.sstewartgallus.plato;

import com.sstewartgallus.ext.variables.Id;

import java.util.function.Function;

/**
 * The high level syntax for the core System F terms in my little language.
 * <p>
 * This is intended to be pristine source language untainted by compiler stuff.
 * <p>
 * Any processing should happen AFTER this step.
 * <p>
 * See https://gitlab.haskell.org/ghc/ghc/-/wikis/commentary/compiler/core-syn-type
 * and https://github.com/DanBurton/Blog/blob/master/Literate%20Haskell/SystemF.lhs
 * for inspiration.
 */
public interface Term<A> {
    static <A, B> Term<B> apply(Term<V<A, B>> f, Type<A> x) {
        return new TypeApplyThunk<>(f, x);
    }

    static <A, B> Term<B> apply(Term<F<A, B>> f, Term<A> x) {
        return new ApplyThunk<>(f, x);
    }

    static <A, B> Term<V<A, B>> v(Function<Type<A>, Term<B>> f) {
        return new TypeLambdaValue<>(f);
    }

    Type<A> type() throws TypeCheckException;

    // fixme... see if it is possible to make variable substitution not part of the core language
    // fixme.. I think I need some kind of visitor API or something...
    default <X> Term<A> substitute(Id<X> variable, Type<X> replacement) {
        throw new UnsupportedOperationException(getClass().toString());
    }

    default <X> Term<A> substitute(Id<X> variable, Term<X> replacement) {
        throw new UnsupportedOperationException(getClass().toString());
    }

    default Term<A> visit(Visitor visitor) {
        return visitor.term(this).visitChildren(visitor);
    }

    default Term<A> visitChildren(Visitor visitor) {
        return this;
    }

    abstract class Visitor {
        public abstract <T> Term<T> term(Term<T> term);
    }
}
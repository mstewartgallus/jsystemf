package com.sstewartgallus.plato;

import com.sstewartgallus.interpreter.Code;

import java.lang.constant.Constable;
import java.util.Optional;
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
public interface Term<A> extends Constable {
    static <A, B> Term<B> apply(Term<V<A, B>> f, Type<A> x) {
        return new TypeApplyTerm<>(f, x);
    }

    static <A, B> Term<B> apply(Term<F<A, B>> f, Term<A> x) {
        return new ApplyTerm<>(f, x);
    }

    static <A, B> ValueTerm<V<A, B>> v(Function<Type<A>, Term<B>> f) {
        return new SimpleTypeLambdaTerm<>(f);
    }

    default Optional<TermDesc<A>> describeConstable() {
        return Optional.empty();
    }

    Type<A> type() throws TypeCheckException;

    default Term<A> visit(Visitor visitor) {
        return visitor.term(this);
    }

    Term<A> visitChildren(Visitor visitor);

    default Code<Term<A>> compile() {
        throw new UnsupportedOperationException(getClass().toString());
    }

    abstract class Visitor {
        public <T> Type<T> type(Type<T> type) {
            return type;
        }

        public <T> Term<T> term(Term<T> term) {
            return term;
        }
    }
}
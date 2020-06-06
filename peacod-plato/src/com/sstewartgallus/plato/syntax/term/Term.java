package com.sstewartgallus.plato.syntax.term;

import com.sstewartgallus.plato.cbpv.Code;
import com.sstewartgallus.plato.runtime.Fun;
import com.sstewartgallus.plato.runtime.U;
import com.sstewartgallus.plato.runtime.V;
import com.sstewartgallus.plato.syntax.ext.variables.VarType;
import com.sstewartgallus.plato.syntax.type.NominalType;
import com.sstewartgallus.plato.syntax.type.Type;

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
 * See http://cs.ioc.ee/efftt/levy-slides.pdf
 */
public interface Term<A> {
    static <A, B> Term<B> apply(Term<V<A, B>> f, Type<A> x) {
        return new TypeApplyTerm<>(f, x);
    }

    static <A, B> Term<B> apply(Term<Fun<U<A>, B>> f, Term<A> x) {
        return new ApplyTerm<>(NominalType.ofTag(new VarType<>()), f, x);
    }

    static <A, B> Term<V<A, B>> v(Function<Type<A>, Term<B>> f) {
        return new TypeLambdaTerm<>(f);
    }

    Type<A> type();

    default Constraints findConstraints() {
        return new Constraints();
    }

    default Term<A> resolve(Solution solution) {
        throw new UnsupportedOperationException(getClass().toString());
    }

    default Term<A> visit(Visitor visitor) {
        return visitor.term(this);
    }

    Term<A> visitChildren(Visitor visitor);

    default Code<A> compile(Environment environment) {
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
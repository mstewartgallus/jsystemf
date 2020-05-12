package com.sstewartgallus.plato;

import java.lang.constant.Constable;
import java.util.function.Function;

// https://gitlab.haskell.org/ghc/ghc/-/wikis/commentary/compiler/core-syn-type
// arguments CoreExpr = Expr Var

// data Expr b	-- "b" for the arguments of binders,
//   = Var	  Id
//   | Lit   Literal
//   | App   (Expr b) (Arg b)
//   | Lam   b (Expr b)
//   | Let   (Bind b) (Expr b)
//   | Case  (Expr b) b Type [Alt b]
//   | Cast  (Expr b) Coercion
//   | Tick  (Tickish Id) (Expr b)
//   | Type  Type

// arguments Arg b = Expr b
// arguments Alt b = (AltCon, [b], Expr b)

// data AltCon = DataAlt DataCon | LitAlt  Literal | DEFAULT

// data Bind b = NonRec b (Expr b) | Rec [(b, (Expr b))]


// https://github.com/DanBurton/Blog/blob/master/Literate%20Haskell/SystemF.lhs

/**
 * The high level syntax for a System F term in my little language.
 * <p>
 * This is intended to be pristine source language untainted by compiler stuff.
 * <p>
 * Any processing should happen AFTER this step.
 *
 * @param <L>
 */
public interface Term<L> {
    static <A, B> Term<B> apply(Term<V<A, B>> f, Type<A> x) {
        return new TypeApplyThunk<>(f, x);
    }

    static <A, B> Term<B> apply(Term<F<A, B>> f, Term<A> x) {
        return new ApplyThunk<>(f, x);
    }

    static <A, B> Term<V<A, B>> v(Function<Type<A>, Term<B>> f) {
        return new TypeLambdaTerm<>(f);
    }

    static <T extends Constable> Term<T> pure(Type<T> type, T value) {
        var constant = value.describeConstable();
        if (constant.isEmpty()) {
            throw new IllegalArgumentException("not a constable value " + value);
        }
        return new PureValue<>(type, constant.get());
    }

    Type<L> type() throws TypeCheckException;
}
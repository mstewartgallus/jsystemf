package com.sstewartgallus.optiimization;

import com.sstewartgallus.ext.tuples.*;
import com.sstewartgallus.plato.*;

interface Args<A extends Tuple<A>, B, C> {

    Signature<A, B, C> sig();

    ValueTerm<A> flatten();

    record NoArgs<A>(Type<A>result) implements Args<N, A, A> {

        @Override
        public Signature<N, A, A> sig() {
            return new Signature.Result<>(result);
        }

        @Override
        public ValueTerm<N> flatten() {
            return NilTupleValue.NIL;
        }
    }

    record AddVar<A, T extends Tuple<T>, B, C>(Term<A>head,
                                               Args<T, B, C>tail) implements Args<P<A, T>, B, F<A, C>> {
        @Override
        public Signature<P<A, T>, B, F<A, C>> sig() {
            return new Signature.AddArg<>(head.type(), tail.sig());
        }

        @Override
        public ValueTerm<P<A, T>> flatten() {
            return new TuplePairValue<>(head, tail.flatten());
        }
    }

}

public final class CurryApply {
    private CurryApply() {
    }

    public static <A> Term<A> curryApply(Term<A> root) {
        return root.visit(new Term.Visitor() {
            @Override
            public <T> Term<T> term(Term<T> term) {
                if (!(term instanceof ApplyThunk<?, T> applyThunk)) {
                    return term.visitChildren(this);
                }
                return curryApply(applyThunk, new Args.NoArgs<>(applyThunk.type()));
            }
        });
    }

    private static <A, B, X extends Tuple<X>, C> Term<C> curryApply(ApplyThunk<A, B> apply, Args<X, C, B> args) {
        var f = apply.f();
        var x = apply.x();

        return curry(f, new Args.AddVar<>(x, args));
    }

    private static <A, B, X extends Tuple<X>, C> Term<C> curry(Term<F<A, B>> term, Args<X, C, F<A, B>> args) {
        if (term instanceof ApplyThunk<?, F<A, B>> fApply) {
            return curryApply(fApply, args);
        }

        term = curryApply(term);

        var sig = args.sig();
        var curryF = new UncurryValue<>(sig);

        var fCurried = Term.apply(curryF, term);
        return Term.apply(fCurried, args.flatten());
    }
}
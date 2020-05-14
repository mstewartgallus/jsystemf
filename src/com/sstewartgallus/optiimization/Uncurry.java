package com.sstewartgallus.optiimization;

import com.sstewartgallus.ext.tuples.HList;
import com.sstewartgallus.ext.tuples.TupleLambdaThunk;
import com.sstewartgallus.plato.Term;

public final class Uncurry {
    private Uncurry() {
    }

    public static <A> Term<A> uncurry(Term<A> root) {
        return root.visit(new Term.Visitor() {
            @Override
            public <T> Term<T> term(Term<T> term) {
                if (!(term instanceof TupleLambdaThunk<?, ?, T> tupleLambda)) {
                    return term.visitChildren(this);
                }
                return uncurryLambda(tupleLambda);
            }
        });
    }

    private static <A extends HList<A>, B, C> Term<C> uncurryLambda(TupleLambdaThunk<A, B, C> lambda) {
        var sig = lambda.sig();
        var f = lambda.f();
        return sig.uncurry(f).toUncurry();
    }
}

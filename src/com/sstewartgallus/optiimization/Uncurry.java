package com.sstewartgallus.optiimization;

import com.sstewartgallus.ext.java.ObjectValue;
import com.sstewartgallus.ext.tuples.CurriedApplyThunk;
import com.sstewartgallus.ext.tuples.HList;
import com.sstewartgallus.ext.tuples.TupleLambdaThunk;
import com.sstewartgallus.ext.variables.VarValue;
import com.sstewartgallus.plato.Term;

public final class Uncurry {
    private Uncurry() {
    }

    public static <A> Term<A> uncurry(Term<A> term) {
        if (term instanceof TupleLambdaThunk<?, ?, A> lambda) {
            return uncurryLambda(lambda);
        }
        if (term instanceof CurriedApplyThunk<A> apply) {
            return uncurryApply(apply);
        }

        if (term instanceof ObjectValue) {
            return term;
        }

        if (term instanceof VarValue<A>) {
            return term;
        }

        throw new IllegalArgumentException("Unexpected core list " + term);
    }

    private static <A> Term<A> uncurryApply(CurriedApplyThunk<A> apply) {
        var uncurriedBody = uncurryApplyBody(apply.body());
        return new CurriedApplyThunk<>(uncurriedBody);
    }

    private static <A> CurriedApplyThunk.Body<A> uncurryApplyBody(CurriedApplyThunk.Body<A> body) {
        if (body instanceof CurriedApplyThunk.MonoBody<A> monoBody) {
            return new CurriedApplyThunk.MonoBody<>(uncurry(monoBody.body()));
        }
        return uncurryApplyBodyApply((CurriedApplyThunk.ApplyBody<?, A>) body);
    }

    private static <A, B> CurriedApplyThunk.Body<B> uncurryApplyBodyApply(CurriedApplyThunk.ApplyBody<A, B> apply) {
        var uncurryF = uncurryApplyBody(apply.f());
        var uncurryX = uncurry(apply.x());
        return new CurriedApplyThunk.ApplyBody<>(uncurryF, uncurryX);
    }

    private static <A extends HList<A>, B, C> Term<C> uncurryLambda(TupleLambdaThunk<A, B, C> lambda) {
        var sig = lambda.sig();
        var f = lambda.f();
        return sig.uncurry(f).toUncurry();
    }
}

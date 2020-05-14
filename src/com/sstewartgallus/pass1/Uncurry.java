package com.sstewartgallus.pass1;

import com.sstewartgallus.plato.*;

public final class Uncurry {
    private Uncurry() {
    }

    public static <A> Term<A> uncurry(Term<A> term, IdGen ids) {
        if (term instanceof TupleLambdaThunk<?, ?, A> lambda) {
            return uncurryLambda(lambda, ids);
        }
        if (term instanceof CurriedApplyThunk<A> apply) {
            return uncurryApply(apply, ids);
        }

        if (!(term instanceof CoreTerm<A> core)) {
            throw new IllegalArgumentException("Unexpected list " + term);
        }

        if (core instanceof PureValue) {
            return core;
        }

        if (core instanceof VarValue<A>) {
            return core;
        }

        throw new IllegalArgumentException("Unexpected core list " + term);
    }

    private static <A> Term<A> uncurryApply(CurriedApplyThunk<A> apply, IdGen ids) {
        var uncurriedBody = uncurryApplyBody(apply.body(), ids);
        return new CurriedApplyThunk<>(uncurriedBody);
    }

    private static <A> CurriedApplyThunk.Body<A> uncurryApplyBody(CurriedApplyThunk.Body<A> body, IdGen ids) {
        if (body instanceof CurriedApplyThunk.MonoBody<A> monoBody) {
            return new CurriedApplyThunk.MonoBody<>(uncurry(monoBody.body(), ids));
        }
        return uncurryApplyBodyApply((CurriedApplyThunk.ApplyBody<?, A>) body, ids);
    }

    private static <A, B> CurriedApplyThunk.Body<B> uncurryApplyBodyApply(CurriedApplyThunk.ApplyBody<A, B> apply, IdGen ids) {
        var uncurryF = uncurryApplyBody(apply.f(), ids);
        var uncurryX = uncurry(apply.x(), ids);
        return new CurriedApplyThunk.ApplyBody<>(uncurryF, uncurryX);
    }

    private static <A extends HList<A>, B, C> Term<C> uncurryLambda(TupleLambdaThunk<A, B, C> lambda, IdGen ids) {
        var sig = lambda.sig();
        var f = lambda.f();
        return sig.uncurry(f, ids).toUncurry();
    }
}

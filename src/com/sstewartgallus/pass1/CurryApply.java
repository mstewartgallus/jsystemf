package com.sstewartgallus.pass1;

import com.sstewartgallus.plato.*;

public final class CurryApply {
    private CurryApply() {
    }

    public static <A> Term<A> curryApply(Term<A> term, IdGen ids) {
        if (term instanceof CurriedLambdaValue<A> lambda) {
            return curryLambda(lambda, ids);
        }

        if (!(term instanceof CoreTerm<A> core)) {
            return term;
        }

        if (core instanceof PureValue || core instanceof VarValue) {
            return core;
        }

        if (core instanceof ApplyThunk<?, A> apply) {
            return curryApply(apply, ids);
        }

        throw new IllegalArgumentException("Unexpected core term " + term);
    }

    private static <A, B> Term<B> curryApply(ApplyThunk<A, B> apply, IdGen ids) {
        var f = curryApply(apply.f(), ids);
        var x = curryApply(apply.x(), ids);
        if (f instanceof CurriedApplyValue<F<A, B>> fCurry) {
            return new CurriedApplyValue<>(new CurriedApplyValue.ApplyBody<>(fCurry.body(), x));
        }
        return new CurriedApplyValue<>(new CurriedApplyValue.ApplyBody<>(new CurriedApplyValue.MonoBody<>(f), x));
    }

    private static <A> Term<A> curryLambda(CurriedLambdaValue<A> lambda, IdGen ids) {
        return new CurriedLambdaValue<>(curryBody(lambda.body(), ids));
    }

    private static <A> CurriedLambdaValue.Body<A> curryBody(CurriedLambdaValue.Body<A> body, IdGen ids) {
        if (body instanceof CurriedLambdaValue.MainBody<A> mainBody) {
            return new CurriedLambdaValue.MainBody<>(curryApply(mainBody.body(), ids));
        }
        return (CurriedLambdaValue.LambdaBody) curryLambdaBody((CurriedLambdaValue.LambdaBody<?, ?>) body, ids);
    }

    private static <A, B> CurriedLambdaValue.LambdaBody<A, B> curryLambdaBody(CurriedLambdaValue.LambdaBody<A, B> body, IdGen ids) {
        var domain = body.domain();
        var v = ids.<A>createId();
        var curriedBody = body.f().apply(new VarValue<>(domain, v));
        return new CurriedLambdaValue.LambdaBody<>(domain, x -> curriedBody.substitute(v, x));
    }
}

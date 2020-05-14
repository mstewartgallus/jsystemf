package com.sstewartgallus.optiimization;

import com.sstewartgallus.ext.java.ObjectValue;
import com.sstewartgallus.ext.tuples.CurriedApplyThunk;
import com.sstewartgallus.ext.tuples.CurriedLambdaThunk;
import com.sstewartgallus.ext.variables.Id;
import com.sstewartgallus.ext.variables.VarValue;
import com.sstewartgallus.plato.ApplyThunk;
import com.sstewartgallus.plato.F;
import com.sstewartgallus.plato.Term;

public final class CurryApply {
    private CurryApply() {
    }

    public static <A> Term<A> curryApply(Term<A> term) {
        if (term instanceof CurriedLambdaThunk<A> lambda) {
            return curryLambda(lambda);
        }

        if (term instanceof ObjectValue || term instanceof VarValue) {
            return term;
        }

        if (term instanceof ApplyThunk<?, A> apply) {
            return curryApply(apply);
        }

        return term;
    }

    private static <A, B> Term<B> curryApply(ApplyThunk<A, B> apply) {
        var f = curryApply(apply.f());
        var x = curryApply(apply.x());
        if (f instanceof CurriedApplyThunk<F<A, B>> fCurry) {
            return new CurriedApplyThunk<>(new CurriedApplyThunk.ApplyBody<>(fCurry.body(), x));
        }
        return new CurriedApplyThunk<>(new CurriedApplyThunk.ApplyBody<>(new CurriedApplyThunk.MonoBody<>(f), x));
    }

    private static <A> Term<A> curryLambda(CurriedLambdaThunk<A> lambda) {
        return new CurriedLambdaThunk<>(curryBody(lambda.body()));
    }

    private static <A> CurriedLambdaThunk.Body<A> curryBody(CurriedLambdaThunk.Body<A> body) {
        if (body instanceof CurriedLambdaThunk.MainBody<A> mainBody) {
            return new CurriedLambdaThunk.MainBody<>(curryApply(mainBody.body()));
        }
        return (CurriedLambdaThunk.LambdaBody) curryLambdaBody((CurriedLambdaThunk.LambdaBody<?, ?>) body);
    }

    private static <A, B> CurriedLambdaThunk.LambdaBody<A, B> curryLambdaBody(CurriedLambdaThunk.LambdaBody<A, B> body) {
        var domain = body.domain();
        var v = new Id<A>();
        var curriedBody = body.f().apply(new VarValue<>(domain, v));
        return new CurriedLambdaThunk.LambdaBody<>(domain, x -> curriedBody.substitute(v, x));
    }
}

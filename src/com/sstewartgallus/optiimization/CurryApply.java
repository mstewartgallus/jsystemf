package com.sstewartgallus.optiimization;

import com.sstewartgallus.ext.java.ObjectValue;
import com.sstewartgallus.ext.tuples.CurriedApplyThunk;
import com.sstewartgallus.ext.tuples.CurriedLambdaThunk;
import com.sstewartgallus.ext.variables.IdGen;
import com.sstewartgallus.ext.variables.VarValue;
import com.sstewartgallus.plato.ApplyThunk;
import com.sstewartgallus.plato.CoreTerm;
import com.sstewartgallus.plato.F;
import com.sstewartgallus.plato.Term;

public final class CurryApply {
    private CurryApply() {
    }

    public static <A> Term<A> curryApply(Term<A> term, IdGen ids) {
        if (term instanceof CurriedLambdaThunk<A> lambda) {
            return curryLambda(lambda, ids);
        }

        if (!(term instanceof CoreTerm<A> core)) {
            return term;
        }

        if (core instanceof ObjectValue || core instanceof VarValue) {
            return core;
        }

        if (core instanceof ApplyThunk<?, A> apply) {
            return curryApply(apply, ids);
        }

        throw new IllegalArgumentException("Unexpected core list " + term);
    }

    private static <A, B> Term<B> curryApply(ApplyThunk<A, B> apply, IdGen ids) {
        var f = curryApply(apply.f(), ids);
        var x = curryApply(apply.x(), ids);
        if (f instanceof CurriedApplyThunk<F<A, B>> fCurry) {
            return new CurriedApplyThunk<>(new CurriedApplyThunk.ApplyBody<>(fCurry.body(), x));
        }
        return new CurriedApplyThunk<>(new CurriedApplyThunk.ApplyBody<>(new CurriedApplyThunk.MonoBody<>(f), x));
    }

    private static <A> Term<A> curryLambda(CurriedLambdaThunk<A> lambda, IdGen ids) {
        return new CurriedLambdaThunk<>(curryBody(lambda.body(), ids));
    }

    private static <A> CurriedLambdaThunk.Body<A> curryBody(CurriedLambdaThunk.Body<A> body, IdGen ids) {
        if (body instanceof CurriedLambdaThunk.MainBody<A> mainBody) {
            return new CurriedLambdaThunk.MainBody<>(curryApply(mainBody.body(), ids));
        }
        return (CurriedLambdaThunk.LambdaBody) curryLambdaBody((CurriedLambdaThunk.LambdaBody<?, ?>) body, ids);
    }

    private static <A, B> CurriedLambdaThunk.LambdaBody<A, B> curryLambdaBody(CurriedLambdaThunk.LambdaBody<A, B> body, IdGen ids) {
        var domain = body.domain();
        var v = ids.<A>createId();
        var curriedBody = body.f().apply(new VarValue<>(domain, v));
        return new CurriedLambdaThunk.LambdaBody<>(domain, x -> curriedBody.substitute(v, x));
    }
}

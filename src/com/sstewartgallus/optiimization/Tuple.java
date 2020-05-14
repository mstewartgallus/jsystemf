package com.sstewartgallus.optiimization;

import com.sstewartgallus.ext.java.ObjectValue;
import com.sstewartgallus.ext.tuples.CurriedApplyThunk;
import com.sstewartgallus.ext.tuples.CurriedLambdaThunk;
import com.sstewartgallus.ext.tuples.HList;
import com.sstewartgallus.ext.tuples.TupleLambdaThunk;
import com.sstewartgallus.ext.variables.Id;
import com.sstewartgallus.ext.variables.IdGen;
import com.sstewartgallus.ext.variables.VarValue;
import com.sstewartgallus.plato.CoreTerm;
import com.sstewartgallus.plato.F;
import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.Type;

public final class Tuple {
    private Tuple() {
    }

    public static <A> Term<A> uncurry(Term<A> term, IdGen ids) {
        if (term instanceof CurriedLambdaThunk<A> lambda) {
            return uncurryLambda(lambda, ids);
        }
        if (term instanceof CurriedApplyThunk<A> apply) {
            return uncurryApply(apply, ids);
        }

        if (!(term instanceof CoreTerm<A> core)) {
            throw new IllegalArgumentException("Unexpected list " + term);
        }

        if (core instanceof ObjectValue) {
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

    private static <A> Term<A> uncurryLambda(CurriedLambdaThunk<A> lambda, IdGen ids) {
        return uncurryBody(lambda.body(), ids);
    }

    private static <X extends HList<X>, A, C> Term<A> uncurryBody(CurriedLambdaThunk.Body<A> body, IdGen ids) {
        if (body instanceof CurriedLambdaThunk.MainBody<A> mainBody) {
            return uncurry(mainBody.body(), ids);
        }
        var lambda = (CurriedLambdaThunk.LambdaBody<?, ?>) body;
        // fixme...
        return (Term) uncurryLambdaBody(lambda, ids);
    }

    private static <X extends HList<X>, B, A, C> Term<F<B, A>> uncurryLambdaBody(CurriedLambdaThunk.LambdaBody<B, A> lambda, IdGen ids) {
        var domain = lambda.domain();
        var f = lambda.f();

        var head = ids.<B>createId();
        var headVar = new VarValue<>(domain, head);

        var body = f.apply(headVar);

        Term<A> toUncurry = uncurryBody(body, ids);
        if (toUncurry instanceof TupleLambdaThunk<?, ?, A> tuple) {
            // fixme...
            return cons(domain, head, (TupleLambdaThunk) tuple);
        }

        return new TupleLambdaThunk<>(new TupleLambdaThunk.Sig.Cons<>(domain, new TupleLambdaThunk.Sig.Zero<>(body.type())), tuple -> {
            var h = tuple.head();
            return toUncurry.substitute(head, h);
        });
    }

    private static <X extends HList<X>, C, B, A> Term<F<B, A>> cons(Type<B> domain, Id<B> head, TupleLambdaThunk<X, F<B, C>, A> tuple) {
        var tupleF = tuple.f();
        var env = tuple.sig();
        TupleLambdaThunk.Sig<HList.Cons<Term<B>, X>, F<B, C>, F<B, A>> sig = new TupleLambdaThunk.Sig.Cons<B, X, F<B, C>, A>(domain, env);
        return new TupleLambdaThunk<HList.Cons<Term<B>, X>, F<B, C>, F<B, A>>(sig, p -> {
            Term<B> h = p.head();
            X t = p.tail();
            return tupleF.apply(t).substitute(head, h);
        });
    }
}

package com.sstewartgallus.pass1;

import com.sstewartgallus.plato.*;

public final class Uncurry {
    private Uncurry() {
    }

    public static <A> Term<A> uncurry(Term<A> term, IdGen ids) {
        if (term instanceof CurriedLambdaValue<A> lambda) {
            return uncurryLambda(lambda, ids);
        }

        if (!(term instanceof CoreTerm<A> core)) {
            throw new IllegalArgumentException("Unexpected term " + term);
        }

        if (core instanceof PureValue) {
            return core;
        }

        if (core instanceof VarValue<A> v) {
            return core;
        }

        if (core instanceof ApplyThunk<?, A> apply) {
            return uncurryApply(apply, ids);
        }

        throw new IllegalArgumentException("Unexpected core term " + term);
    }

    private static <A, B> Term<B> uncurryApply(ApplyThunk<A, B> apply, IdGen ids) {
        var uncurryF = uncurry(apply.f(), ids);
        var uncurryX = uncurry(apply.x(), ids);
        return new ApplyThunk<>(uncurryF, uncurryX);
    }

    private static <A> Term<A> uncurryLambda(CurriedLambdaValue<A> lambda, IdGen ids) {
        return uncurryBody(lambda.body(), ids, NilNormal.NIL);
    }

    private static <Env extends HList<Env>, A> Term<A> uncurryBody(CurriedLambdaValue.Body<A> body, IdGen ids, Type<Env> env) {
        if (body instanceof CurriedLambdaValue.MainBody<A> mainBody) {
            var uncurryBody = uncurry(mainBody.body(), ids);
            return uncurryBody;
        }
        var lambda = (CurriedLambdaValue.LambdaBody<?, ?>) body;
        // fixme...
        return (Term) uncurryLambdaBody(lambda, ids, (Type) env);
    }

    private static <X extends HList<X>, A, B> Term<F<A, F<X, B>>> uncurryLambdaBody(CurriedLambdaValue.LambdaBody<A, B> lambda, IdGen ids, Type<X> env) {
        var domain = lambda.domain();
        var f = lambda.f();

        var head = ids.<A>createId();
        var headVar = new VarValue<>(domain, head);

        var body = f.apply(headVar);

        Term<B> toUncurry = uncurryBody(body, ids, new ConsNormal<>(domain, env));
        if (toUncurry instanceof UncurryValue<?, ?, ?> uncurry) {
            return cons((UncurryValue) uncurry);
        }
        return new UncurryValue<>(domain, new ConsNormal<>(domain, env).l(x -> toUncurry.substitute(head, head(x))));
    }

    private static <X extends HList<X>, A, B> Term<F<A, F<X, B>>> cons(UncurryValue<X, A, B> uncurry) {
        return null;
    }

    private static <A, Env extends HList<Env>> Term<A> head(Term<HList.Cons<A, Env>> product) {
        return new HeadThunk<>(product);
    }
}

package com.sstewartgallus.optiimization;

import com.sstewartgallus.ext.tuples.Cons;
import com.sstewartgallus.ext.tuples.CurriedLambdaThunk;
import com.sstewartgallus.ext.tuples.HList;
import com.sstewartgallus.ext.tuples.TupleLambdaThunk;
import com.sstewartgallus.ext.variables.Id;
import com.sstewartgallus.ext.variables.VarValue;
import com.sstewartgallus.plato.F;
import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.Type;

public final class Tuple {
    private Tuple() {
    }

    public static <A> Term<A> uncurry(Term<A> root) {
        return root.visit(new Term.Visitor() {
            @Override
            public <T> Term<T> term(Term<T> term) {
                if (!(term instanceof CurriedLambdaThunk<T> lambdaThunk)) {
                    return term.visitChildren(this);
                }
                return uncurryLambda(lambdaThunk);
            }
        });
    }

    private static <A> Term<A> uncurryLambda(CurriedLambdaThunk<A> lambda) {
        return uncurryBody(lambda.body());
    }

    private static <X extends HList<X>, A, C> Term<A> uncurryBody(CurriedLambdaThunk.Body<A> body) {
        if (body instanceof CurriedLambdaThunk.MainBody<A> mainBody) {
            return uncurry(mainBody.body());
        }
        var lambda = (CurriedLambdaThunk.LambdaBody<?, ?>) body;
        // fixme...
        return (Term) uncurryLambdaBody(lambda);
    }

    private static <X extends HList<X>, B, A, C> Term<F<B, A>> uncurryLambdaBody(CurriedLambdaThunk.LambdaBody<B, A> lambda) {
        var domain = lambda.domain();
        var f = lambda.f();

        var head = new Id<B>();
        var headVar = new VarValue<>(domain, head);

        var body = f.apply(headVar);

        Term<A> toUncurry = uncurryBody(body);
        if (toUncurry instanceof TupleLambdaThunk<?, ?, A> tuple) {
            // fixme...
            return cons(domain, head, (TupleLambdaThunk) tuple);
        }

        return new TupleLambdaThunk<>(new TupleLambdaThunk.Sig.Cons<>(domain, new TupleLambdaThunk.Sig.Zero<>(body.type())), tuple -> {
            var h = tuple.head();
            return new VarValue<>(h.type(), head).substituteIn(toUncurry, h);
        });
    }

    private static <X extends HList<X>, C, B, A> Term<F<B, A>> cons(Type<B> domain, Id<B> head, TupleLambdaThunk<X, F<B, C>, A> tuple) {
        var tupleF = tuple.f();
        var env = tuple.sig();
        TupleLambdaThunk.Sig<Cons<Term<B>, X>, F<B, C>, F<B, A>> sig = new TupleLambdaThunk.Sig.Cons<B, X, F<B, C>, A>(domain, env);
        return new TupleLambdaThunk<Cons<Term<B>, X>, F<B, C>, F<B, A>>(sig, p -> {
            Term<B> h = p.head();
            X t = p.tail();
            return new VarValue<>(h.type(), head).substituteIn(tupleF.apply(t), h);
        });
    }
}

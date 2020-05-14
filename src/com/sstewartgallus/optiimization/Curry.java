package com.sstewartgallus.optiimization;

import com.sstewartgallus.ext.tuples.CurriedLambdaThunk;
import com.sstewartgallus.ext.variables.Id;
import com.sstewartgallus.ext.variables.VarValue;
import com.sstewartgallus.plato.F;
import com.sstewartgallus.plato.LambdaValue;
import com.sstewartgallus.plato.Term;

public final class Curry {
    private Curry() {
    }

    // fixme.. avoid the IdGen state capture issues..
    public static <A> Term<A> curry(Term<A> root) {
        return root.visit(new Term.Visitor() {
            @Override
            public <T> Term<T> term(Term<T> term) {
                if (!(term instanceof LambdaValue<?, ?> lambdaValue)) {
                    return term.visitChildren(this);
                }
                return (Term) curryLambda(lambdaValue);
            }
        });
    }

    private static <A, B> Term<F<A, B>> curryLambda(LambdaValue<A, B> lambda) {
        var domain = lambda.domain();
        var f = lambda.f();

        var v = new Id<A>();
        var body = f.apply(new VarValue<>(domain, v));

        var curriedBody = curry(body);

        if (curriedBody instanceof CurriedLambdaThunk<B> curriedLambdaValue) {
            var expr = curriedLambdaValue.body();
            return new CurriedLambdaThunk<>(new CurriedLambdaThunk.LambdaBody<>(domain, x -> expr.substitute(v, x)));
        }

        return new CurriedLambdaThunk<>(new CurriedLambdaThunk.LambdaBody<>(domain, x -> new CurriedLambdaThunk.MainBody<>(body.substitute(v, x))));
    }
}

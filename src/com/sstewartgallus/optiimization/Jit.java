package com.sstewartgallus.optiimization;

import com.sstewartgallus.plato.F;
import com.sstewartgallus.plato.SimpleLambdaValue;
import com.sstewartgallus.plato.Term;

public final class Jit {
    private Jit() {
    }

    public static <A> Term<A> jit(Term<A> root) {
        return root.visit(new Term.Visitor() {
            @Override
            public <T> Term<T> term(Term<T> term) {
                if (!(term instanceof SimpleLambdaValue<?, ?> lambda)) {
                    return term.visitChildren(this);
                }
                return (Term) pointFreeify(lambda);
            }
        });
    }

    private static <A, B> Term<F<A, B>> pointFreeify(SimpleLambdaValue<A, B> lambda) {
        return lambda.jit();
    }
}

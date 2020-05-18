package com.sstewartgallus.optiimization;

import com.sstewartgallus.ext.variables.VarValue;
import com.sstewartgallus.plato.F;
import com.sstewartgallus.plato.SimpleLambdaValue;
import com.sstewartgallus.plato.Term;

/**
 * Converting to point free form is like taking the derivative of a function.
 * <p>
 * PointFree[ K (x, y) ]_x  = K (1, y)
 * <p>
 * d xy / dx = 1 * y
 */
public final class ConvertPointFree {
    private ConvertPointFree() {
    }

    // fixme... how to typecheck
    public static <A> Term<A> pointFree(Term<A> root) {
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
        var domain = lambda.domain();

        var v = new VarValue<>(domain);
        var body = lambda.apply(v);
        return body.pointFree(v);
    }
}

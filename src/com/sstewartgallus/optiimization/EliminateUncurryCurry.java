package com.sstewartgallus.optiimization;

import com.sstewartgallus.ext.tuples.CurryThunk;
import com.sstewartgallus.ext.tuples.UncurryThunk;
import com.sstewartgallus.plato.ApplyThunk;
import com.sstewartgallus.plato.Term;

public final class EliminateUncurryCurry {
    private EliminateUncurryCurry() {
    }

    // fixme... how to typecheck
    public static <A> Term<A> eliminate(Term<A> root) {
        return root.visit(new Term.Visitor() {
            @Override
            public <T> Term<T> term(Term<T> term) {
                if (term instanceof ApplyThunk<?, T> apply) {
                    return eliminateApply(apply);
                }
                return term.visitChildren(this);
            }
        });
    }

    private static <A, B> Term<B> eliminateApply(ApplyThunk<A, B> apply) {
        var f = eliminate(apply.f());
        var x = eliminate(apply.x());

        if (!(f instanceof UncurryThunk uncurryThunk)) {
            return new ApplyThunk<>(f, x);
        }

        if (!(x instanceof ApplyThunk xApply && xApply.f() instanceof CurryThunk curryThunk)) {
            return new ApplyThunk<>(f, x);
        }

        // fixme... do type safely.
        if (!curryThunk.signature().equals(uncurryThunk.signature())) {
            return new ApplyThunk<>(f, x);
        }

        return (Term) xApply.x();
    }

}

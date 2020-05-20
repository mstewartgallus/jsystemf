package com.sstewartgallus.optimizers;


import com.sstewartgallus.ext.tuples.CurryValue;
import com.sstewartgallus.ext.tuples.UncurryValue;
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

        if (!(f instanceof UncurryValue uncurryThunk)) {
            return new ApplyThunk<>(f, x);
        }

        if (!(x instanceof ApplyThunk xApply && xApply.f() instanceof CurryValue curryValue)) {
            return new ApplyThunk<>(f, x);
        }

        // fixme... do type safely.
        if (!curryValue.signature().equals(uncurryThunk.signature())) {
            return new ApplyThunk<>(f, x);
        }

        return (Term) xApply.x();
    }

}

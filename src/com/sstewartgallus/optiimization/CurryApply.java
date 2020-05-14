package com.sstewartgallus.optiimization;

import com.sstewartgallus.ext.tuples.CurriedApplyThunk;
import com.sstewartgallus.plato.ApplyThunk;
import com.sstewartgallus.plato.F;
import com.sstewartgallus.plato.Term;

public final class CurryApply {
    private CurryApply() {
    }

    public static <A> Term<A> curryApply(Term<A> root) {
        return root.visit(new Term.Visitor() {
            @Override
            public <T> Term<T> term(Term<T> term) {
                if (!(term instanceof ApplyThunk<?, T> applyThunk)) {
                    return term.visitChildren(this);
                }
                return curryApply(applyThunk);
            }
        });
    }

    private static <A, B> Term<B> curryApply(ApplyThunk<A, B> apply) {
        var f = curryApply(apply.f());
        var x = curryApply(apply.x());
        if (f instanceof CurriedApplyThunk<F<A, B>> fCurry) {
            return new CurriedApplyThunk<>(new CurriedApplyThunk.ApplyBody<>(fCurry.body(), x));
        }
        return new CurriedApplyThunk<>(new CurriedApplyThunk.ApplyBody<>(new CurriedApplyThunk.MonoBody<>(f), x));
    }
}

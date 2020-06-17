package com.sstewartgallus;

import com.sstewartgallus.plato.ir.systemf.*;
import com.sstewartgallus.plato.runtime.Fn;
import com.sstewartgallus.plato.runtime.U;

public class EtaReduction {
    public static <A> Term<A> etaReduction(Term<A> term) {
        if (term instanceof LambdaTerm<?, ?> lambdaTerm) {
            return (Term) lambda(lambdaTerm);
        }
        if (term instanceof ApplyTerm<?, A> apply) {
            return apply(apply);
        }
        return term;
    }

    private static <B, A> Term<A> apply(ApplyTerm<B, A> term) {
        var f = etaReduction(term.f());
        var x = etaReduction(term.x());
        return new ApplyTerm<>(f, x);
    }

    private static <A, B> Term<Fn<U<A>, B>> lambda(LambdaTerm<A, B> term) {
        var binder = term.binder();
        var body = etaReduction(term.body());
        if (body instanceof ApplyTerm<?, B> applyTerm) {
            if (applyTerm.x() instanceof LocalTerm<?> lit && lit.variable().equals(binder)) {
                if (!contains(applyTerm.f(), binder)) {
                    return (Term) applyTerm.f();
                }
            }
        }
        return new LambdaTerm<>(binder, body);
    }

    private static <C> boolean contains(Term<C> term, Variable<?> x) {
        if (term instanceof LocalTerm<?> localTerm && localTerm.variable().equals(x)) {
            return true;
        }
        if (term instanceof LambdaTerm<?, ?> lambdaTerm) {
            return contains(lambdaTerm.body(), x);
        }
        if (term instanceof ApplyTerm<?, C> apply) {
            return contains(apply.f(), x) || contains(apply.x(), x);
        }
        return false;
    }
}

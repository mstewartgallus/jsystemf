package com.sstewartgallus.plato.compiler;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.systemf.ApplyTerm;
import com.sstewartgallus.plato.ir.systemf.LambdaTerm;
import com.sstewartgallus.plato.ir.systemf.LocalTerm;
import com.sstewartgallus.plato.ir.systemf.Term;
import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.Fn;
import com.sstewartgallus.plato.runtime.type.Stk;

public class EtaReduceTerm {
    public static <A> Term<A> etaReduce(Term<A> term) {
        if (term instanceof LambdaTerm<?, ?> lambdaTerm) {
            return (Term) lambda(lambdaTerm);
        }
        if (term instanceof ApplyTerm<?, A> apply) {
            return apply(apply);
        }
        return term;
    }

    private static <B, A> Term<A> apply(ApplyTerm<B, A> term) {
        var f = etaReduce(term.f());
        var x = etaReduce(term.x());
        return new ApplyTerm<>(f, x);
    }

    private static <A, B> Term<Fn<Stk<F<Stk<A>>>, B>> lambda(LambdaTerm<A, B> term) {
        var binder = term.binder();
        var body = etaReduce(term.body());
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
        return term.contains(x) > 0;
    }
}

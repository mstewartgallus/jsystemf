package com.sstewartgallus.plato.compiler;

import com.sstewartgallus.plato.ir.systemf.*;
import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.Fn;
import com.sstewartgallus.plato.runtime.type.Stk;

public class InlineTerm {
    public static <A> Term<A> inline(Term<A> term) {
        return inline(new TermMap(), term);
    }

    private static <A> Term<A> inline(TermMap env, Term<A> term) {
        if (term instanceof ApplyTerm<?, A> applyTerm) {
            return apply(env, applyTerm);
        }
        if (term instanceof LambdaTerm<?, ?> lambdaTerm) {
            return (Term) lambda(env, lambdaTerm);
        }
        if (term instanceof LocalTerm<A> localTerm) {
            return localTerm(env, localTerm);
        }
        return term.visitChildren(new TermVisitor() {
            @Override
            public <C> Term<C> onTerm(Term<C> term) {
                return inline(env, term);
            }
        });
    }

    private static <A> Term<A> localTerm(TermMap env, LocalTerm<A> localTerm) {
        var replacement = env.get(localTerm.variable());
        if (replacement == null) {
            return localTerm;
        }
        return replacement;
    }

    private static <B, A> Term<A> apply(TermMap env, ApplyTerm<B, A> term) {
        var f = inline(env, term.f());
        var x = inline(env, term.x());
        if (f instanceof LambdaTerm<B, A> lambdaTerm) {
            var binder = lambdaTerm.binder();
            var body = lambdaTerm.body();
            if (body.contains(binder) <= 1) {
                return inline(env.put(binder, x), body);
            }
        }
        return new ApplyTerm<>(f, x);
    }

    private static <A, B> Term<Fn<Stk<F<Stk<A>>>, B>> lambda(TermMap env, LambdaTerm<A, B> term) {
        var binder = term.binder();
        var body = inline(env.clear(binder), term.body());
        return new LambdaTerm<>(binder, body);
    }
}
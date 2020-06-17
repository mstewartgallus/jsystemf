package com.sstewartgallus;

import com.sstewartgallus.plato.ir.systemf.*;
import com.sstewartgallus.plato.runtime.Fn;
import com.sstewartgallus.plato.runtime.U;

import java.util.Map;
import java.util.TreeMap;

public class InlineTerm {
    public static <A> Term<A> inline(Term<A> term) {
        return inline(new Environment(Map.of()), term);
    }

    private static <A> Term<A> inline(Environment env, Term<A> term) {
        if (term instanceof LambdaTerm<?, ?> lambdaTerm) {
            return (Term) lambda(env, lambdaTerm);
        }
        if (term instanceof ApplyTerm<?, A> apply) {
            return apply(env, apply);
        }
        if (term instanceof LocalTerm<A> localTerm) {
            return localTerm(env, localTerm);
        }
        return term;
    }

    private static <A> Term<A> localTerm(Environment env, LocalTerm<A> localTerm) {
        var replacement = env.get(localTerm.variable());
        if (replacement == null) {
            return localTerm;
        }
        return replacement;
    }

    private static <B, A> Term<A> apply(Environment env, ApplyTerm<B, A> term) {
        var f = inline(env, term.f());
        var x = inline(env, term.x());
        if (f instanceof LambdaTerm<B, A> lambdaTerm) {
            var binder = lambdaTerm.binder();
            var body = lambdaTerm.body();
            if (contains(body, binder) <= 1) {
                return inline(env.put(binder, x), body);
            }
        }
        return new ApplyTerm<>(f, x);
    }

    private static <A, B> Term<Fn<U<A>, B>> lambda(Environment env, LambdaTerm<A, B> term) {
        var binder = term.binder();
        var body = inline(env.clear(binder), term.body());
        return new LambdaTerm<>(binder, body);
    }

    private static <C> int contains(Term<C> term, Variable<?> x) {
        if (term instanceof LocalTerm<?> localTerm && localTerm.variable().equals(x)) {
            return 1;
        }
        if (term instanceof LambdaTerm<?, ?> lambdaTerm) {
            // fixme... what about variable scoping ?
            return contains(lambdaTerm.body(), x);
        }
        if (term instanceof ApplyTerm<?, C> apply) {
            return contains(apply.f(), x) + contains(apply.x(), x);
        }
        return 0;
    }

    private static record Environment(Map<Variable, Term>variables) {

        public <A> Environment clear(Variable<A> binder) {
            var copy = new TreeMap<>(variables);
            copy.remove(binder);
            return new Environment(copy);
        }

        public <A> Term<A> get(Variable<U<A>> variable) {
            return variables.get(variable);
        }

        public <A> Environment put(Variable<U<A>> binder, Term<A> f) {
            var copy = new TreeMap<>(variables);
            copy.put(binder, f);
            return new Environment(copy);
        }
    }
}

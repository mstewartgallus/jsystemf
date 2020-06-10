package com.sstewartgallus;

import com.sstewartgallus.plato.ir.cbpv.*;
import com.sstewartgallus.plato.ir.systemf.*;
import com.sstewartgallus.plato.runtime.Fn;
import com.sstewartgallus.plato.runtime.U;

public class TermToCbpv {
    public static <A> Literal<U<A>> toCbpv(Term<A> term) {
        if (term instanceof LambdaTerm<?, ?> lambdaTerm) {
            return (Literal) lambda(lambdaTerm);
        }
        if (term instanceof ApplyTerm<?, A> apply) {
            return apply(apply);
        }
        if (term instanceof GlobalTerm<A> global) {
            return global(global);
        }
        if (term instanceof LocalTerm<A> local) {
            return localLiteral(local);
        }
        throw new IllegalArgumentException(term.toString());
    }

    private static <A> Literal<U<A>> global(GlobalTerm<A> term) {
        return new GlobalLiteral<>(term.type().thunk(), term.packageName(), term.name());
    }

    private static <B, A> Literal<U<A>> apply(ApplyTerm<B, A> term) {
        var f = force(toCbpv(term.f()));
        var x = toCbpv(term.x());
        return apply(f, x);
    }

    private static <B, A> Literal<U<A>> apply(Code<Fn<U<B>, A>> f, Literal<U<B>> x) {
        if (f instanceof LambdaCode<U<B>, A> lambdaCode) {
            return thunk(new LetBeCode<>(lambdaCode.binder(), x, lambdaCode.body()));
        }
        return thunk(new ApplyCode<>(f, x));
    }

    private static <A, B> Literal<U<Fn<U<A>, B>>> lambda(LambdaTerm<A, B> term) {
        var binder = localLiteral(term.binder());
        var body = force(toCbpv(term.body()));
        return thunk(new LambdaCode<>(binder, body));
    }

    private static <A> LocalLiteral<U<A>> localLiteral(LocalTerm<A> binder) {
        return new LocalLiteral<>(binder.type().thunk(), binder.name());
    }

    private static <A> Literal<U<A>> thunk(Code<A> code) {
        if (code instanceof ForceCode<A> forceCode) {
            return forceCode.thunk();
        }
        return new ThunkLiteral<>(code);
    }

    private static <A> Code<A> force(Literal<U<A>> thunk) {
        if (thunk instanceof ThunkLiteral<A> thunkLiteral) {
            return thunkLiteral.code();
        }
        return new ForceCode<>(thunk);
    }
}

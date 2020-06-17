package com.sstewartgallus;

import com.sstewartgallus.plato.ir.cbpv.*;
import com.sstewartgallus.plato.ir.systemf.*;
import com.sstewartgallus.plato.java.IntLiteral;
import com.sstewartgallus.plato.java.IntTerm;
import com.sstewartgallus.plato.runtime.Fn;
import com.sstewartgallus.plato.runtime.U;

public class TermToCbpv {
    public static <A> Code<A> toCbpv(Term<A> term) {
        if (term instanceof LambdaTerm<?, ?> lambdaTerm) {
            return (Code) lambda(lambdaTerm);
        }
        if (term instanceof ApplyTerm<?, A> apply) {
            return apply(apply);
        }
        if (term instanceof GlobalTerm<A> global) {
            return global(global);
        }
        if (term instanceof LocalTerm<A> local) {
            return force(new LocalLiteral<>(local.variable()));
        }
        if (term instanceof IntTerm intTerm) {
            return (Code) new ReturnCode<>(new IntLiteral(intTerm.value()));
        }
        throw new IllegalArgumentException(term.toString());
    }

    private static <A> Code<A> global(GlobalTerm<A> term) {
        return new GlobalCode<>(term.global());
    }

    private static <B, A> Code<A> apply(ApplyTerm<B, A> term) {
        var f = toCbpv(term.f());
        var x = thunk(toCbpv(term.x()));
        return apply(f, x);
    }

    private static <B, A> Code<A> apply(Code<Fn<U<B>, A>> f, Literal<U<B>> x) {
        if (f instanceof LambdaCode<U<B>, A> lambdaCode) {
            return new LetBeCode<>(lambdaCode.binder(), x, lambdaCode.body());
        }
        return new ApplyCode<>(f, x);
    }

    private static <A, B> Code<Fn<U<A>, B>> lambda(LambdaTerm<A, B> term) {
        var body = toCbpv(term.body());
        return new LambdaCode<>(term.binder(), body);
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

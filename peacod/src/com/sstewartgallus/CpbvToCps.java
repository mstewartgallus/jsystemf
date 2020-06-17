package com.sstewartgallus;

import com.sstewartgallus.plato.ir.cbpv.*;
import com.sstewartgallus.plato.ir.cps.*;
import com.sstewartgallus.plato.java.IntLiteral;
import com.sstewartgallus.plato.java.IntValue;
import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.Fn;
import com.sstewartgallus.plato.runtime.U;

import java.util.function.Function;

public class CpbvToCps {
    public static <A> Action<A> cps(Code<A> code) {
        var lbl = Lbl.newInstance(code.type());

        return KontAction.of(lbl, cps(code, act -> new ApplyKont<>(act, new GotoKont<A>(lbl))));
    }

    private static <A> Instr cps(Code<A> term, Function<Action<A>, Instr> k) {
        if (term instanceof LambdaCode lambdaCode) {
            return lambda(lambdaCode, (Function) k);
        }
        if (term instanceof ApplyCode<?, A> apply) {
            return apply(apply, k);
        }
        if (term instanceof GlobalCode<A> global) {
            return global(global, k);
        }
        if (term instanceof ForceCode forceCode) {
            return force(forceCode, k);
        }
        if (term instanceof ReturnCode retCode) {
            return returnCode(retCode, (Function) k);
        }
        if (term instanceof LetToCode<?, A> letToCode) {
            return letToCode(letToCode, k);
        }
        if (term instanceof LetBeCode<?, A> letBeCode) {
            return letBeCode(letBeCode, k);
        }
        throw new IllegalArgumentException(term.getClass().toString());
    }

    private static <A> Instr cps(Literal<A> lit, Function<Value<A>, Instr> k) {
        if (lit instanceof IntLiteral intLiteral) {
            return k.apply((Value) new IntValue(intLiteral.value()));
        }
        if (lit instanceof LocalLiteral<A> localLiteral) {
            return k.apply(new LocalValue<>(localLiteral.variable()));
        }
        if (lit instanceof ThunkLiteral thunkLiteral) {
            return thunk(thunkLiteral, (Function) k);
        }
        throw new IllegalArgumentException(lit.toString());
    }

    private static <A, B> Instr letBeCode(LetBeCode<B, A> letToCode, Function<Action<A>, Instr> k) {
        var binder = letToCode.binder();
        var body = letToCode.body();

        return cps(letToCode.value(), action -> new ApplyKont<>(new ReturnAction<>(action), LetToKont.of(binder, cps(body, theBody -> k.apply(theBody)))));
    }

    private static <A, B> Instr letToCode(LetToCode<B, A> letToCode, Function<Action<A>, Instr> k) {
        var binder = letToCode.binder();
        var body = letToCode.body();

        return cps(letToCode.action(), action -> new ApplyKont<>(action, LetToKont.of(binder, cps(body, theBody -> k.apply(theBody)))));
    }

    private static <A> Instr returnCode(ReturnCode<A> retCode, Function<Action<F<A>>, Instr> k) {
        return cps(retCode.literal(), val -> k.apply(new ReturnAction<>(val)));
    }

    private static <A> Instr force(ForceCode<A> forceCode, Function<Action<A>, Instr> k) {
        var code = forceCode.thunk();
        return cps(code, val -> k.apply(new ForceAction<>(val)));
    }

    private static <A> Instr thunk(ThunkLiteral<A> thunkLiteral, Function<Value<U<A>>, Instr> k) {
        var body = thunkLiteral.code();

        return cps(body, x -> k.apply(new ThunkValue<>(x)));
    }

    private static <B, A> Instr apply(ApplyCode<B, A> term, Function<Action<A>, Instr> k) {
        var type = term.type();
        return cps(term.x(), value -> {
            Lbl<A> jump = Lbl.newInstance(type);

            var t = new PushKont<>(value, new GotoKont<>(jump));

            return k.apply(KontAction.of(jump, cps(term.f(), fValue -> new ApplyKont<>(fValue, t))));
        });
    }

    private static <A> Instr global(GlobalCode<A> term, Function<Action<A>, Instr> k) {
        return k.apply(new GlobalAction<>(term.global()));
    }

    private static <A, B> Instr lambda(LambdaCode<A, B> term, Function<Action<Fn<A, B>>, Instr> k) {
        var binder = term.binder();
        var body = term.body();

        var jump = Lbl.newInstance(body.type());

        return k.apply(new LambdaAction<A, B>(binder, KontAction.of(jump, cps(body, bodyVal -> new ApplyKont<>(bodyVal, new GotoKont<>(jump))))));
    }
}

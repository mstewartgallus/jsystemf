package com.sstewartgallus;

import com.sstewartgallus.plato.ir.cbpv.*;
import com.sstewartgallus.plato.ir.cps.*;
import com.sstewartgallus.plato.java.IntLiteral;
import com.sstewartgallus.plato.java.IntValue;
import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.Fn;
import com.sstewartgallus.plato.runtime.U;

public class DeCps {

    public static <A> Code<A> deCps(Action<A> stack) {
        var env = new Environment();

        return deCps(env, stack);
    }

    // fixme... solve this later...
    private static <A> Literal<A> deCps(Environment env, Value<A> value) {
        if (value instanceof ThunkValue thunk) {
            return decpsThunkValue(env, thunk);
        }
        if (value instanceof IntValue intValue) {
            return (Literal) decpsIntValue(env, intValue);
        }
        if (value instanceof LocalValue<A> localValue) {
            return decpsLocalValue(env, localValue);
        }
        throw new IllegalArgumentException(value.getClass().toString());
    }

    private static <A> Literal<A> decpsLocalValue(Environment env, LocalValue<A> localValue) {
        return new LocalLiteral<>(localValue.variable());
    }

    private static Literal<Integer> decpsIntValue(Environment env, IntValue intValue) {
        return new IntLiteral(intValue.value());
    }

    private static <A> Literal<U<A>> decpsThunkValue(Environment env, ThunkValue<A> thunk) {
        var jump = deCps(env, thunk.action());

        return new ThunkLiteral<>(jump);
    }


    private static <A, B> Code<B> deCpsStack(Environment env, Code<A> action, Kont<A> stack) {
        if (stack instanceof LetToKont letToKont) {
            return dCpsLetToKont(env, (Code) action, letToKont);
        }
        if (stack instanceof GotoKont<A> gotoKont) {
            return dCpsGotoKont(env, action, gotoKont);
        }
        if (stack instanceof PushKont pushKont) {
            return dCpsPushKont(env, (Code) action, pushKont);
        }
        throw new IllegalArgumentException(stack.getClass().toString());
    }

    private static <B, A, C> Code<C> dCpsPushKont(Environment env, Code<Fn<A, B>> action, PushKont<A, B> pushKont) {
        var value = deCps(env, pushKont.head());
        var apply = new ApplyCode<A, B>(action, value);
        return deCpsStack(env, apply, pushKont.tail());
    }

    private static <B, A> Code<B> dCpsGotoKont(Environment env, Code<A> action, GotoKont<A> gotoKont) {
        return new BreakCode<>(gotoKont.label(), action);
    }

    private static <A, B> Code<B> deCpsStack(Environment env, Instr stack) {
        if (stack instanceof ApplyKont<?> applyKont) {
            return dCpsApplyKont(env, applyKont);
        }
        throw new IllegalArgumentException(stack.getClass().toString());
    }

    private static <B, C> Code<B> dCpsLetToKont(Environment env, Code<F<C>> action, LetToKont<C> letToKont) {
        var variable = letToKont.variable();
        return LetToCode.of(variable, action, deCpsStack(env, letToKont.body()));
    }

    private static <B, A> Code<B> dCpsApplyKont(Environment env, ApplyKont<A> applyKont) {
        var x = deCps(env, applyKont.action());
        return deCpsStack(env, x, applyKont.next());
    }

    // fixme... not at all type safe...
    private static <A> Code<A> deCps(Environment env, Action<A> action) {
        if (action instanceof GlobalAction<A> globalAction) {
            return decpsGlobalAction(env, globalAction);
        }
        if (action instanceof ForceAction<A> forceAction) {
            return decpsForceAction(env, forceAction);
        }
        if (action instanceof ReturnAction returnAction) {
            return decpsReturnAction(env, returnAction);
        }
        if (action instanceof LambdaAction lambdaAction) {
            return decpsLambdaAction(env, lambdaAction);
        }
        if (action instanceof KontAction<A> kontAction) {
            return decpsKontAction(env, kontAction);
        }
        throw new IllegalArgumentException(action.getClass().toString());
    }

    private static <A> Code<A> decpsKontAction(Environment env, KontAction<A> kontAction) {
        return new CallccCode<>(kontAction.label(), deCpsStack(env, kontAction.body()));
    }

    private static <A, B> Code<Fn<A, B>> decpsLambdaAction(Environment env, LambdaAction<A, B> lambdaAction) {
        var binder = lambdaAction.variable();

        var jump = deCps(env, lambdaAction.body());
        return new LambdaCode<A, B>(binder, jump);
    }

    private static <A> Code<F<A>> decpsReturnAction(Environment env, ReturnAction<A> returnAction) {
        return new ReturnCode<>(deCps(env, returnAction.value()));
    }

    private static <A> Code<A> decpsForceAction(Environment env, ForceAction<A> forceAction) {
        return new ForceCode<>(deCps(env, forceAction.thunk()));
    }

    private static <A> Code<A> decpsGlobalAction(Environment env, GlobalAction<A> globalAction) {
        return new GlobalCode<>(globalAction.global());
    }

    private static record Environment() {

    }
}

package com.sstewartgallus;

import com.sstewartgallus.plato.ir.cps.*;
import com.sstewartgallus.plato.java.IntValue;
import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.Fn;
import com.sstewartgallus.plato.runtime.U;

import java.util.Map;
import java.util.TreeMap;

public class LiftCps {
    public static <A> Action<A> inline(Action<A> term) {
        for (; ; ) {
            var opt = lift(new Environment(Map.of()), term);
            if (opt.equals(term)) {
                return term;
            }
            term = opt;
        }
    }

    private static <A> Value<A> inline(Environment env, Value<A> value) {
        if (value instanceof ThunkValue thunk) {
            return inlineThunkValue(env, thunk);
        }
        if (value instanceof IntValue intValue) {
            return (Value) inlineIntValue(env, intValue);
        }
        if (value instanceof LocalValue<A> localValue) {
            return inlineLocalValue(env, localValue);
        }
        throw new IllegalArgumentException(value.getClass().toString());
    }

    private static <A> Value<A> inlineLocalValue(Environment env, LocalValue<A> localValue) {
        return new LocalValue<>(localValue.variable());
    }

    private static Value<Integer> inlineIntValue(Environment env, IntValue intValue) {
        return new IntValue(intValue.value());
    }

    private static <A> Value<U<A>> inlineThunkValue(Environment env, ThunkValue<A> thunk) {
        var jump = lift(env, thunk.action());

        return new ThunkValue<>(jump);
    }

    private static Instr inlineStack(Environment env, Instr stack) {
        if (stack instanceof ApplyKont<?> applyKont) {
            return inlineApplyKont(env, applyKont);
        }
        throw new IllegalArgumentException(stack.getClass().toString());
    }

    private static <B> Kont<B> inlineStack(Environment env, Kont<B> stack) {
        if (stack instanceof Instr instr) {
            return (Kont) inlineStack(env, instr);
        }
        if (stack instanceof PushKont pushKont) {
            return inlinePushKont(env, pushKont);
        }
        if (stack instanceof GotoKont<B> gotoKont) {
            return inlineGotoKont(env, gotoKont);
        }
        if (stack instanceof LetToKont letToKont) {
            return inlineLetToKont(env, letToKont);
        }
        throw new IllegalArgumentException(stack.getClass().toString());
    }

    private static <B> Kont<F<B>> inlineLetToKont(Environment env, LetToKont<B> letToKont) {
        var binder = letToKont.variable();
        var body = letToKont.body();
        return new LetToKont<>(binder, inlineStack(env, body));
    }

    private static <B> Kont<B> inlineGotoKont(Environment env, GotoKont<B> gotoKont) {
        var replacement = env.get(gotoKont.label());
        if (replacement != null) {
            return replacement;
        }
        return gotoKont;
    }

    private static <A, B> Kont<Fn<A, B>> inlinePushKont(Environment env, PushKont<A, B> pushKont) {
        var head = inline(env, pushKont.head());
        var tail = inlineStack(env, pushKont.tail());

        return new PushKont<>(head, tail);
    }

    private static <A> Instr inlineApplyKont(Environment env, ApplyKont<A> applyKont) {
        var x = lift(env, applyKont.action());
        var f = inlineStack(env, applyKont.next());
        return new ApplyKont<>(x, f);
    }

    private static <A> Action<A> lift(Environment env, Action<A> action) {
        if (action instanceof GlobalAction<A> globalAction) {
            return inlineGlobalAction(env, globalAction);
        }
        if (action instanceof ForceAction<A> forceAction) {
            return inlineForceAction(env, forceAction);
        }
        if (action instanceof ReturnAction returnAction) {
            return inlineReturnAction(env, returnAction);
        }
        if (action instanceof LambdaAction lambdaAction) {
            return inlineLambdaAction(env, lambdaAction);
        }
        if (action instanceof KontAction<A> kontAction) {
            return inlineKontAction(env, kontAction);
        }
        throw new IllegalArgumentException(action.getClass().toString());
    }

    private static <A> Action<A> inlineKontAction(Environment env, KontAction<A> kontAction) {
        var label = kontAction.label();
        var body = inlineStack(env, kontAction.body());
        if (body instanceof ApplyKont<?> applyKont) {
            var action = applyKont.action();
            var next = applyKont.next();
            if (next instanceof GotoKont<?> gotoKont && gotoKont.label().equals(label)) {
                System.err.println("fixme... check if label is not captured in body");
                return (Action) action;
            }
        }
        return new KontAction<A>(label, body);
    }

    private static <A, B> Action<Fn<A, B>> inlineLambdaAction(Environment env, LambdaAction<A, B> lambdaAction) {
        var binder = lambdaAction.variable();

        var body = lift(env, lambdaAction.body());
        return new LambdaAction<A, B>(binder, body);
    }

    private static <A> Action<F<A>> inlineReturnAction(Environment env, ReturnAction<A> returnAction) {
        return new ReturnAction<>(inline(env, returnAction.value()));
    }

    private static <A> Action<A> inlineForceAction(Environment env, ForceAction<A> forceAction) {
        return new ForceAction<>(inline(env, forceAction.thunk()));
    }

    private static <A> Action<A> inlineGlobalAction(Environment env, GlobalAction<A> globalAction) {
        return new GlobalAction<>(globalAction.global());
    }

    private static record Environment(Map<Lbl, Kont>labels) {

        public <A> Kont<A> get(Lbl<A> label) {
            return labels.get(label);
        }

        public <A> Environment put(Lbl<A> label, Kont<A> f) {
            var copy = new TreeMap<>(labels);
            copy.put(label, f);
            return new Environment(copy);
        }
    }
}

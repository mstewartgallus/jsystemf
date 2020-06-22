package com.sstewartgallus.plato.compiler;

import com.sstewartgallus.plato.ir.cps.*;
import com.sstewartgallus.plato.runtime.type.Behaviour;

public class InlineCps {
    public static <A> Value<A> inline(Value<A> action) {
        return inline(new LabelMap(), action);
    }

    private static <A> Action<A> inline(LabelMap env, Action<A> action) {
        if (action instanceof ApplyStackAction applyStackAction) {
            action = inlineApply(env, applyStackAction);
        }
        if (action instanceof LetBeAction<?, A> letBeAction) {
            action = inlineLetBe(env, letBeAction);
        }
        return action.visitChildren(new ActionVisitor() {
            @Override
            public <C> Action<C> onAction(Action<C> action) {
                return inline(env, action);
            }
        }, new ValueVisitor() {
            @Override
            public <C> Value<C> onValue(Value<C> value) {
                return inline(env, value);
            }
        });
    }

    private static <B, A> Action<A> inlineLetBe(LabelMap env, LetBeAction<B, A> letBeAction) {
        var variable = letBeAction.binder();
        var value = letBeAction.value();
        var body = letBeAction.body();

        System.err.println("check for multiple use of variable");
        return inline(env.put(variable, value), body);
    }

    private static <C> Value<C> inline(LabelMap env, Value<C> value) {
        if (value instanceof LocalValue<C> local) {
            var label = local.variable();
            var result = env.get(label);
            if (result != null) {
                value = result;
            }
        }
        if (value instanceof StackLabelValue jumpAction) {
            var label = jumpAction.label();
            var result = (Value<C>) env.get(label);
            if (result != null) {
                value = result;
            }
        }
        return value.visitChildren(new ActionVisitor() {
            @Override
            public <C> Action<C> onAction(Action<C> action) {
                return inline(env, action);
            }
        }, new ValueVisitor() {
            @Override
            public <C> Value<C> onValue(Value<C> value) {
                return inline(env, value);
            }
        });
    }

    private static <A> Action<Behaviour> inlineApply(LabelMap env, ApplyStackAction<A> applyStackAction) {
        var action = applyStackAction.action();
        var next = applyStackAction.next();

        if (!(action instanceof KontAction<A> kontAction)) {
            return applyStackAction;
        }
        var label = kontAction.label();
        var actionBody = kontAction.action();

        System.err.println("check for multiple use of continuation");
        return inline(env.put(label, next), actionBody);
    }

}

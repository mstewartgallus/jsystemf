package com.sstewartgallus;

import com.sstewartgallus.plato.ir.cbpv.*;
import com.sstewartgallus.plato.ir.cps.Lbl;
import com.sstewartgallus.plato.ir.systemf.Global;
import com.sstewartgallus.plato.ir.systemf.Variable;
import com.sstewartgallus.plato.ir.type.Type;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.java.IntLiteral;
import com.sstewartgallus.plato.runtime.*;

import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class CodeInterpreter {
    public static <A> U<A> interpret(Code<A> action, MethodHandles.Lookup lookup) {
        var env = new Environment(lookup, Map.of(), Map.of());
        return new U<A>() {
            @Override
            public <C> C accept(Stack<C, A> stack) throws Control {
                return interpret(action, stack, env);
            }
        };
    }

    private static <A> A interpret(Literal<A> value, Environment environment) {
        if (value instanceof ThunkLiteral thunkLiteral) {
            return (A) thunk(thunkLiteral, environment);
        }
        if (value instanceof IntLiteral intLiteral) {
            return (A) (Integer) intLiteral.value();
        }
        if (value instanceof LocalLiteral<A> localLiteral) {
            return environment.get(localLiteral.variable());
        }
        throw new IllegalArgumentException(value.getClass().toString());
    }


    private static <A, C> C interpret(Code<A> action, Stack<C, A> stack, Environment environment) throws Control {
        if (action instanceof ForceCode forceCode) {
            return (C) force(forceCode, stack, environment);
        }
        if (action instanceof ApplyCode<?, A> applyCode) {
            return apply(applyCode, stack, environment);
        }
        if (action instanceof GlobalCode<A> globalCode) {
            return global(globalCode, stack, environment);
        }
        if (action instanceof LambdaCode lambdaCode) {
            return (C) lambda(lambdaCode, (Stack) stack, environment);
        }
        if (action instanceof CallccCode<A> callccCode) {
            return callccCode(callccCode, stack, environment);
        }
        if (action instanceof LetToCode<?, A> letToCode) {
            return letToCode(letToCode, stack, environment);
        }
        if (action instanceof BreakCode<?, A> breakCode) {
            return breakCode(breakCode, stack, environment);
        }
        throw new IllegalArgumentException(action.getClass().toString());
    }

    private static <C, B, A> C breakCode(BreakCode<B, A> breakCode, Stack<C, A> stack, Environment environment) throws Control {
        var label = breakCode.label();
        var arg = breakCode.argument();

        var value = interpret(arg, environment.getLabel(label), environment);

        var breakControl = new BreakControl();
        breakControl.label = label;
        breakControl.value = value;
        throw breakControl;
    }

    private static <C, B, A> C letToCode(LetToCode<B, A> letToCode, Stack<C, A> stack, Environment environment) throws Control {
        var variable = letToCode.binder();
        var value = interpret(letToCode.action(), new FreeStack<>(), environment);
        return interpret(letToCode.body(), stack, environment.put(variable, value));
    }

    private static <C, A> C callccCode(CallccCode<A> callccCode, Stack<C, A> stack, Environment environment) throws Control {
        var label = callccCode.label();
        var contents = callccCode.contents();

        try {
            return interpret(contents, stack, environment.putLabel(label, stack));
        } catch (BreakControl breakControl) {
            if (breakControl.label == label) {
                return (C) breakControl.value;
            }
            throw breakControl;
        }
    }

    private static <A, B, C> C lambda(LambdaCode<A, B> lambdaCode, Stack<C, Fn<A, B>> stack, Environment environment) throws Control {
        var variable = lambdaCode.binder();
        var body = lambdaCode.body();

        // fixme... I don't believe needs to check..
        if (stack instanceof FnStack<C, A, B> fnstack) {
            return interpret(body, fnstack.next(), environment.put(variable, fnstack.value()));
        }
        throw new IllegalArgumentException(stack.getClass().toString());
    }

    private static <A, C> C global(GlobalCode<A> global, Stack<C, A> stack, Environment environment) {
        return environment.call(global.global(), stack);
    }

    private static <A, B, C> C apply(ApplyCode<A, B> applyCode, Stack<C, B> stack, Environment environment) throws Control {
        var value = interpret(applyCode.x(), environment);
        return interpret(applyCode.f(), new FnStack<>(value, stack), environment);
    }

    private static <A, C> C force(ForceCode<A> forceCode, Stack<C, A> stack, Environment environment) throws Control {
        var th = interpret(forceCode.thunk(), environment);
        return th.accept(stack);
    }

    private static <A> U<A> thunk(ThunkLiteral<A> thunkLiteral, Environment environment) {
        throw null;
    }

    public record Environment(MethodHandles.Lookup lookup, Map<Lbl, Stack>labels, Map<Variable, Object>values) {
        private static final Object SENTINEL = new Object();

        public <A> A get(Variable<A> variable) {
            var value = values.getOrDefault(variable, SENTINEL);
            if (value == SENTINEL) {
                throw new IllegalArgumentException("Variable " + variable + " not found in " + values);
            }
            return (A) value;
        }

        public <A> Environment put(Variable<A> variable, A value) {
            var copy = new TreeMap<>(values);
            copy.put(variable, value);
            return new Environment(lookup, labels, copy);
        }

        public <A> U<A> call(Global<A> global) {
            throw null;
        }

        public <C, A> Environment putLabel(Lbl<A> label, Stack<C, A> stack) {
            var copy = new TreeMap<>(labels);
            copy.put(label, stack);
            return new Environment(lookup, copy, values);
        }

        // fixme...
        public <C, A> Stack<C, A> getLabel(Lbl<A> label) {
            return labels.get(label);
        }

        public <A> Type<A> resolve(TypeDesc<A> type) {
            try {
                return type.resolveConstantDesc(lookup);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

        public <C, A> C call(Global<A> global, Stack<C, A> stack) {
            // fixme.. get arg types from stack...
            var desc = ActionDesc.callGlobal(global.packageName(), global.name(), MethodTypeDesc.of(ConstantDescs.CD_Class));

            var args = new ArrayList<>();
            while (stack instanceof FnStack fnStack) {
                args.add(fnStack.value());
                stack = fnStack.next();
            }
            try {
                return (C) desc.resolveCallSiteDesc(lookup).dynamicInvoker().invokeWithArguments(stack);
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }

        private <C> C doAdd(Stack<C, Fn<Integer, Fn<Integer, F<Integer>>>> stack) {
            var fnstack = (FnStack<C, Integer, Fn<Integer, F<Integer>>>) stack;
            var x = fnstack.value();
            var next = (FnStack<C, Integer, F<Integer>>) fnstack.next();
            return (C) (Integer) (x + next.value());
        }
    }
}
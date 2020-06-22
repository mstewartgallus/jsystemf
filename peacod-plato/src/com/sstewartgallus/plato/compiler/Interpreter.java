package com.sstewartgallus.plato.compiler;

import com.sstewartgallus.plato.ir.Global;
import com.sstewartgallus.plato.ir.Label;
import com.sstewartgallus.plato.ir.NumberConstant;
import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.cps.*;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.*;
import com.sstewartgallus.plato.runtime.type.Behaviour;
import com.sstewartgallus.plato.runtime.type.Stk;
import com.sstewartgallus.plato.runtime.type.Type;
import com.sstewartgallus.plato.runtime.type.U;

import java.lang.invoke.MethodHandles;

public class Interpreter {

    // fixme... consider parameterizing Stack with <C>
    public static <A> A interpret(Value<A> value, MethodHandles.Lookup lookup) {
        var env = new Environment(lookup, new ThunkMap(), new ValueMap());
        return interpretValue(env, value);
    }

    private static <A> A interpretValue(Environment environment, Value<A> value) {
        record Util() {
            static <A> A dispatch(Environment environment, Value<A> value) {
                if (value instanceof ConstantValue<A> constantValue) {
                    return constantValue(constantValue);
                }
                if (value instanceof LocalValue<A> localValue) {
                    return environment.get(localValue.variable());
                }
                if (value instanceof GlobalValue<A> globalValue) {
                    return global(environment, globalValue);
                }
                if (value instanceof SimpleLambdaValue simpleLambdaValue) {
                    return (A) simpleLambdaValue(environment, simpleLambdaValue);
                }
                if (value instanceof StackLabelValue stackLabelValue) {
                    return (A) stackLabelValue(environment, stackLabelValue);
                }
                throw new IllegalArgumentException(value.getClass().toString());
            }


        }
        var result = Util.dispatch(environment, value);
        //   System.err.println(environment + " " + value + " => value " + result);
        return result;
    }

    private static <A> Stk<A> stackLabelValue(Environment environment, StackLabelValue<A> stackLabelValue) {
        var label = stackLabelValue.label();
        return environment.get(label);
    }

    private static <A> Stk<F<A>> simpleLambdaValue(Environment environment, SimpleLambdaValue<A> simpleLambdaValue) {
        var binder = simpleLambdaValue.label();
        var action = simpleLambdaValue.action();
        return new FreeStk<A>() {
            @Override
            public <C> void enter(Continuation<C> context, A value) {
                interpretAction(context, environment.put(binder, value), action, NilStk.NIL);
            }

            @Override
            public String toString() {
                return environment + " & κ " + binder + " →\n" + action;
            }
        };
    }


    private static <A> A constantValue(ConstantValue<A> constantValue) {
        var constant = constantValue.constant();
        if (constant instanceof NumberConstant number) {
            return (A) (Integer) number.value().intValueExact();
        }
        throw new IllegalArgumentException(constantValue.getClass().toString());
    }

    private static <C, A> void interpretAction(Continuation<C> context, Environment environment, Action<A> action, Stk<A> stack) {
        record Utils() {
            static <A> void dispatch(Continuation<C> context, Environment environment, Action<A> action, Stk<A> stack) {
                if (action instanceof ApplyStackAction applyStackAction) {
                    applyStackAction(context, environment, applyStackAction, (Stk) stack);
                    return;
                }
                if (action instanceof ReturnAction returnAction) {
                    returnAction(context, environment, returnAction, (Stk) stack);
                    return;
                }
                if (action instanceof LetToAction<?, A> letToAction) {
                    letToAction(context, environment, letToAction, stack);
                    return;
                }
                if (action instanceof ApplyAction<?, A> applyAction) {
                    applyAction(context, environment, applyAction, stack);
                    return;
                }
                if (action instanceof KontAction<A> kontAction) {
                    kontAction(context, environment, kontAction, stack);
                    return;
                }
                throw new IllegalArgumentException(action.getClass().toString());
            }
        }
        Utils.dispatch(context, environment, action, stack);
    }

    private static <C, A> void kontAction(Continuation<C> context, Environment environment, KontAction<A> kontAction, Stk<A> stack) {
        var label = kontAction.label();
        var action = kontAction.action();

        interpretAction(context, environment.put(label, stack), action, NilStk.NIL);
    }

    private static <C, B, A> void applyAction(Continuation<C> context, Environment environment, ApplyAction<B, A> applyAction, Stk<A> stack) {
        var x = interpretValue(environment, applyAction.x());
        interpretAction(context, environment, applyAction.f(), new PushStk<>(x, stack));
    }

    private static <A, B, C> void letToAction(Continuation<C> context, Environment environment, LetToAction<B, A> letToAction, Stk<A> stack) {
        var action = letToAction.action();
        var binder = letToAction.binder();
        var body = letToAction.body();

        interpretAction(context, environment, action, new FreeStk<B>() {
            @Override
            public <C> void enter(Continuation<C> k, B value) {
                interpretAction(k, environment.put(binder, value), body, stack);
            }

            @Override
            public String toString() {
                return "to " + binder + ".\n" + body + " :: " + stack;
            }
        });
    }

    private static <A, C> void returnAction(Continuation<C> context, Environment environment, ReturnAction<A> returnAction, Stk<F<A>> stack) {
        var value = interpretValue(environment, returnAction.value());
        context.saveOrEnter(stack, value);
    }

    private static <B, C> void applyStackAction(Continuation<C> context, Environment environment, ApplyStackAction<B> apply, Stk<Behaviour> stack) {
        var next = interpretValue(environment, apply.next());
        interpretAction(context, environment, apply.action(), next);
    }

    private static <A> A global(Environment environment, GlobalValue<A> global) {
        return environment.getGlobal(global.global());
    }


    private static <A> Stk<F<Stk<Fn<A, F<A>>>>> getIdentity() {
        // fixme....  preallocate as a constant ?
     /*   return new U<>() {
            @Override
            public <C> void accept(Context<C> context, Stack<C, Fn<A, F<A>>> stack) {
                var fnstack = (FnStack<C, A, F<A>>) stack;
                return fnstack.next().apply(context, new F<>(fnstack.action()));
            }

            @Override
            public String toString() {
                return "I";
            }
        };
   */
        throw null;
    }

    private static <A> Stk<F<Stk<Fn<Variable<U<A>>, Fn<Action<A>, Fn<Environment, A>>>>>> getFixThunk() {
      /*  return new U<>() {
            @Override
            public <C> void accept(Context<C> context, Stack<C, Fn<Variable<U<A>>, Fn<Action<A>, Fn<Environment, A>>>> stack) {
                var fnstack1 = (FnStack<C, Variable<U<A>>, Fn<Action<A>, Fn<Environment, A>>>) stack;
                var variable = fnstack1.action();
                var fnstack2 = (FnStack<C, Action<A>, Fn<Environment, A>>) fnstack1.next();
                var body = fnstack2.action();
                var fnstack3 = (FnStack<C, Environment, A>) fnstack2.next();
                var env = fnstack3.action();
                var next = fnstack3.next();

                var self = this;
                U<A> thunk = new U<A>() {
                    @Override
                    public <C> void accept(Context<C> context, Stack<C, A> stack) {
                        return context.become(self, new FnStack<>(variable, new FnStack<>(body, new FnStack<>(env, stack))));
                    }
                };
                return interpret(context, env.put(variable, thunk), body, next);
            }

            @Override
            public String toString() {
                return "fix";
            }
        };*/
        throw null;
    }

    private static <A> Stk<F<Stk<Fn<Label<A>, Fn<Instr, Fn<Environment, A>>>>>> getLabelThunk() {
        throw null;
        /*        return new U<>() {
            @Override
            public <C> void accept(Context<C> context, Stack<C, Fn<Label<A>, Fn<Instr, Fn<Environment, A>>>> stack) {
                var fnstack1 = (FnStack<C, Label<A>, Fn<Instr, Fn<Environment, A>>>) stack;
                var label = fnstack1.action();
                var fnstack2 = (FnStack<C, Instr, Fn<Environment, A>>) fnstack1.next();
                var body = fnstack2.action();
                var fnstack3 = (FnStack<C, Environment, A>) fnstack2.next();
                var env = fnstack3.action();
                var next = fnstack3.next();

                return interpret(context, env.put(label, next), body);
            }

            @Override
            public String toString() {
                return "eval";
            }
        }; */
    }

    private static <A, B> U<Fn<Environment, Fn<Variable<A>, Fn<Action<B>, Fn<A, B>>>>> getLambdaThunk() {
        throw null;
    }

    public record Environment(MethodHandles.Lookup lookup, ThunkMap labels, ValueMap values) {
        @Override
        public String toString() {
            return "(env " + labels + " " + values + " " + lookup + ")";
        }

        public <A> A get(Variable<A> variable) {
            return values.get(variable);
        }

        public <A> Environment put(Variable<A> variable, A value) {
            return new Environment(lookup, labels, values.put(variable, value));
        }

        public <A> Type<A> resolve(TypeDesc<A> type) {
            try {
                return type.resolveConstantDesc(lookup);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

        public <A> A getGlobal(Global<A> global) {
//            var typeDesc = global.type();

            //          var type = resolve(typeDesc);

            // fixme... grab type erasure..
            return (A) ActionBootstraps.ofReference(lookup, global.name(), Object.class, global.packageName(), null);
        }

        public <A> Stk<A> get(Label<A> label) {
            return labels.get(label);
        }

        public <A> Environment put(Label<A> label, Stk<A> value) {
            return new Environment(lookup, labels.put(label, value), values);
        }
    }

}

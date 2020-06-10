package com.sstewartgallus.plato.runtime;


import com.sstewartgallus.plato.ir.type.Type;
import com.sstewartgallus.plato.java.IntF;
import com.sstewartgallus.plato.java.IntType;
import jdk.dynalink.StandardNamespace;
import jdk.dynalink.StandardOperation;

import java.lang.invoke.*;

import static java.lang.invoke.MethodHandles.insertArguments;
import static java.lang.invoke.MethodHandles.lookup;

@SuppressWarnings("unused")
public final class ActionBootstraps {
    private static final Type<U<IntF>> INT_BOX;
    private static final Type<Integer> INT;
    private static final MethodHandle CLOSURE_FACT_MH;

    static {
        try {
            INT_BOX = IntType.INTF_TYPE.thunk().resolveConstantDesc(lookup());
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        try {
            INT = IntType.INT_TYPE.resolveConstantDesc(lookup());
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        try {
            CLOSURE_FACT_MH = lookup().findStatic(ActionBootstraps.class, "uglyVarargs", MethodType.methodType(U.class, MethodHandleInfo.class, Type.class, MethodHandle.class, U[].class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private ActionBootstraps() {
    }

    /**
     * Fixme... separate out getting the label and calling it on the environment/thunk..
     */
    @SuppressWarnings("unused")
    public static CallSite invoke(MethodHandles.Lookup lookup, String name, MethodType methodType, MethodType desc) {
        var operation = switch (name) {
            case "CALL" -> StandardOperation.CALL;
            case "APPLY" -> StandardOperation.GET
                    .withNamespace(StandardNamespace.METHOD)
                    .named(desc);
            default -> throw new IllegalArgumentException(name);
        };
        return ActionLinker.link(lookup, operation, methodType);
    }

    // fixme.. limit to package private somehow...
    @SuppressWarnings("unused")
    public static <A extends U> A ofMethod(MethodHandles.Lookup lookup, String name, Class<A> klass, Type<A> type, MethodHandle lambdaBody) {
        var info = lookup.revealDirect(lambdaBody);
        return klass.cast(new JitStatic<>(info, type, lambdaBody));
    }

    @SuppressWarnings("unused")
    public static CallSite closureFactory(MethodHandles.Lookup lookup, String name, MethodType methodType, Type<?> type, MethodHandle lambdaBody) {
        var reveal = lookup.revealDirect(lambdaBody);
        return new ConstantCallSite(insertArguments(CLOSURE_FACT_MH, 0, reveal, type, lambdaBody).asCollector(U[].class, methodType.parameterCount()));
    }

    private static U<?> uglyVarargs(MethodHandleInfo info, Type<?> type, MethodHandle lambdaBody, U<?>... env) {
        System.err.println("fixme... closures are awful");
        // fixme... awfull...
        return new JitStatic<>(info, type, insertArguments(lambdaBody, 0, env));
    }

    // fixme... check types...
    @SuppressWarnings("unused")
    public static <A extends U> A ofReference(MethodHandles.Lookup lookup, String name, Class<A> klass, String packageName, Type<A> type) {
        // fixme.. use lookup...
        return switch (packageName) {
            case "" -> throw new IllegalArgumentException(packageName);
            case "core" -> switch (name) {
                case "+" -> (A) getPlus();
                case "-" -> (A) getMinus();
                case "boxInt" -> (A) getBoxInt();
                default -> throw new Error("real field lookup not implemented! tried looking up " + packageName + "/" + name);
            };
            case "foo" -> throw new Error("foo");
            default -> throw new Error("real field lookup not implemented! tried looking up " + packageName + "/" + name);
        };
    }

    private static FnImpl<U<IntF>, Fn<U<IntF>, IntF>> getPlus() {
        return new FnImpl<>(INT_BOX) {
            @Override
            public U<Fn<U<IntF>, IntF>> apply(U<IntF> x) {
                return new FnImpl<>(INT_BOX) {
                    @Override
                    public U<IntF> apply(U<IntF> y) {
                        var left = U.evaluateInteger(x);
                        var right = U.evaluateInteger(y);
                        return IntF.of(left + right);
                    }
                };
            }
        };
    }

    private static FnImpl<U<IntF>, Fn<U<IntF>, IntF>> getMinus() {
        return new FnImpl<>(INT_BOX) {
            @Override
            public U<Fn<U<IntF>, IntF>> apply(U<IntF> x) {
                return new FnImpl<>(INT_BOX) {
                    @Override
                    public U<IntF> apply(U<IntF> y) {
                        var left = U.evaluateInteger(x);
                        var right = U.evaluateInteger(y);
                        return IntF.of(left - right);
                    }
                };
            }
        };
    }

    private static FnImpl<Integer, IntF> getBoxInt() {
        return new FnImpl<>(INT) {
            @Override
            public U<IntF> apply(Integer value) {
                return IntF.of(value);
            }
        };
    }

}

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
    private static final MethodHandle PLUS_MH;

    static {
        try {
            PLUS_MH = lookup().findStatic(ActionBootstraps.class, "add", MethodType.methodType(int.class, int.class, int.class));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

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

    private static int add(int left, int right) {
        return left + right;
    }

    /**
     * Fixme... separate out getting the binder and calling it on the binder/binder..
     */
    @SuppressWarnings("unused")
    public static CallSite invoke(MethodHandles.Lookup lookup, String name, MethodType methodType) {
        var operation = switch (name) {
            case "CALL" -> StandardOperation.CALL;
            case "LABEL" -> StandardOperation.GET
                    .withNamespace(StandardNamespace.METHOD);
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


    @SuppressWarnings("unused")
    public static <A> CallSite bootstrap(MethodHandles.Lookup lookup, String typeOfCall, MethodType methodType, String packageName, String name) {
        if (!(typeOfCall.equals("TAILCALL") || typeOfCall.equals("CALL"))) {
            return null;
        }
        // fixme... check if tail call and wrap in evaluator loop..
        return switch (packageName) {
            case "" -> throw new IllegalArgumentException(packageName);
            case "core" -> switch (name) {
                case "+" -> getPlus();
                case "-" -> throw new Error("impl");
                case "boxInt" -> throw new Error("impl");
                default -> throw new Error("real field lookup not implemented! tried looking up " + packageName + "/" + name);
            };
            case "foo" -> throw new Error("foo");
            default -> throw new Error("real field lookup not implemented! tried looking up " + packageName + "/" + name);
        };
    }

    // fixme... check types...
    @SuppressWarnings("unused")
    public static <A> A ofReference(MethodHandles.Lookup lookup, String name, Class<A> klass, String packageName, Type<A> type) {
        throw null;
    }

    private static CallSite getPlus() {
        return new ConstantCallSite(PLUS_MH);
    }

    private static FnImpl<U<IntF>, Fn<U<IntF>, IntF>> getMinus() {
        throw null;

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

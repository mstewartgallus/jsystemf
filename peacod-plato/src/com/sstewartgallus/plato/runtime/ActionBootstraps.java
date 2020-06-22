package com.sstewartgallus.plato.runtime;


import com.sstewartgallus.plato.java.IntF;
import com.sstewartgallus.plato.runtime.type.Type;
import com.sstewartgallus.plato.runtime.type.U;
import jdk.dynalink.StandardNamespace;
import jdk.dynalink.StandardOperation;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@SuppressWarnings("unused")
public final class ActionBootstraps {

    private ActionBootstraps() {
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

    private static U<Fn<Integer, Fn<Integer, F<Integer>>>> getNil() {
        throw null;
    }

    private static U<Fn<Integer, Fn<Integer, F<Integer>>>> getPlusUnboxed() {
        return new FnImpl<>(null) {
            @Override
            public U<Fn<Integer, F<Integer>>> apply(Integer x) {
                return new FnImpl<>(null) {
                    @Override
                    public U<F<Integer>> apply(Integer y) {
                        return U.box(x + y);
                    }

                    @Override
                    public String toString() {
                        return "(+ " + x + ")";
                    }
                };
            }

            @Override
            public String toString() {
                return "+";
            }
        };
    }

    public static <A> A ofReference(MethodHandles.Lookup lookup, String name, Class<A> klass, String packageName, Type<?> type) {
// fixme... check if tail call and wrap in evaluator loop..
        var result = switch (packageName) {
            case "" -> throw new IllegalArgumentException(packageName);
            case "core" -> switch (name) {
                case "+!" -> getPlusUnboxed();
                case "+" -> getPlus();
                case "nil" -> getNil();


                case "-" -> throw new Error("impl");
                default -> throw new Error("real field lookup not implemented! tried looking up " + packageName + "/" + name);
            };
            case "foo" -> throw new Error("foo");
            default -> throw new Error("real field lookup not implemented! tried looking up " + packageName + "/" + name);
        };
        return klass.cast(result);
    }

    private static CallSite getPlus() {
        throw null;
    }

    private static FnImpl<U<IntF>, Fn<U<IntF>, IntF>> getMinus() {
        throw null;

    }
}

package com.sstewartgallus.plato.runtime;


import com.sstewartgallus.plato.syntax.type.Type;
import jdk.dynalink.StandardOperation;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@SuppressWarnings("unused")
public final class ActionBootstraps {
    private ActionBootstraps() {
    }

    @SuppressWarnings("unused")
    public static CallSite invoke(MethodHandles.Lookup lookup, String name, MethodType methodType) {
        if (!name.equals("CALL")) {
            return null;
        }
        return ActionLinker.link(lookup, StandardOperation.CALL, methodType);
    }

    // fixme... eliminate with BSM_INVOKE if possible...
    // fixme.. limit to package private somehow...
    @SuppressWarnings("unused")
    public static <A extends U> A ofMethod(MethodHandles.Lookup lookup, String name, Class<A> klass, Type<A> type, MethodHandle lambdaBody) {
        return klass.cast(new JitAction<>(name, type, lambdaBody));
    }
}

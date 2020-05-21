package com.sstewartgallus.runtime;


import com.sstewartgallus.ext.mh.JitValue;
import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.Type;
import jdk.dynalink.StandardOperation;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@SuppressWarnings("unused")
public final class TermBootstraps {
    private TermBootstraps() {
    }

    @SuppressWarnings("unused")
    public static CallSite invoke(MethodHandles.Lookup lookup, String name, MethodType methodType) {
        if (!name.equals("CALL")) {
            return null;
        }
        return TermLinker.link(lookup, StandardOperation.CALL, methodType);
    }

    // fixme... eliminate with BSM_INVOKE if possible...
    // fixme.. limit to package private somehow...
    @SuppressWarnings("unused")
    public static <A> Term<A> ofMethod(MethodHandles.Lookup lookup, String name, Class<?> klass, Type<A> type, MethodHandle lambdaBody) {
        return new JitValue<>(name, type, lambdaBody);
    }
}

package com.sstewartgallus.runtime;


import com.sstewartgallus.ext.mh.JitValue;
import com.sstewartgallus.plato.F;
import com.sstewartgallus.plato.FunctionType;
import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.Type;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public final class TermBootstraps {
    private TermBootstraps() {
    }

    // fixme... eliminate with BSM_INVOKE if possible...
    // fixme.. limit to package private somehow...
    @SuppressWarnings("unused")
    public static <A> Term<A> ofMethod(MethodHandles.Lookup lookup, String name, Class<?> klass, Type<A> type, MethodHandle lambdaBody) {
        return new JitValue<>(name, type, lambdaBody);
    }

    @SuppressWarnings("unused")
    public static <A, B> Type<F<A, B>> ofFunction(MethodHandles.Lookup lookup, String name, Class<?> klass, Type<A> domain, Type<B> range) {
        return new FunctionType<>(domain, range);
    }
}

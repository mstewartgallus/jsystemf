package com.sstewartgallus.plato.ir.type;

import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.Fn;
import com.sstewartgallus.plato.runtime.U;
import com.sstewartgallus.plato.runtime.V;

import java.lang.constant.ClassDesc;

public final class TypeDescs {
    private static final String TYPE_PACKAGE = TypeDescs.class.getPackageName();
    public static final ClassDesc CD_Type = ClassDesc.of(TYPE_PACKAGE, "Type");
    public static final ClassDesc CD_TypeBootstraps = ClassDesc.of(TYPE_PACKAGE, "TypeBootstraps");
    private TypeDescs() {
    }

    public static <A, B> TypeDesc<V<A, V<B, Fn<A, B>>>> fn() {
        return TypeDesc.ofReference("core", "fn");
    }

    // fixme... not sure this makes sense...
    public static <A, B> TypeDesc<V<A, V<B, Fn<U<A>, B>>>> fun() {
        return TypeDesc.ofReference("core", "fun");
    }

    public static <A> TypeDesc<V<A, U<A>>> thunk() {
        return TypeDesc.ofReference("core", "u");
    }

    public static <A> TypeDesc<V<A, F<A>>> returns() {
        return TypeDesc.ofReference("core", "f");
    }
}

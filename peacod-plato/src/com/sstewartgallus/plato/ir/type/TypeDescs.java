package com.sstewartgallus.plato.ir.type;

import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.Fn;
import com.sstewartgallus.plato.runtime.V;
import com.sstewartgallus.plato.runtime.type.Stk;
import com.sstewartgallus.plato.runtime.type.U;

public final class TypeDescs {
    private TypeDescs() {
    }

    public static <A, B> TypeDesc<V<A, V<B, Fn<A, B>>>> fn() {
        return TypeDesc.ofReference("core", "fn");
    }

    // fixme... not sure this makes sense...
    public static <A, B> TypeDesc<V<A, V<B, Fn<U<A>, B>>>> fun() {
        return TypeDesc.ofReference("core", "fun");
    }

    // fixme...
    public static <A> TypeDesc<V<A, Stk<F<Stk<A>>>>> thunk() {
        return TypeDesc.ofReference("core", "u");
    }

    public static <A> TypeDesc<V<A, F<A>>> returns() {
        return TypeDesc.ofReference("core", "f");
    }
}

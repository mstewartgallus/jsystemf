package com.sstewartgallus.plato.runtime;

import com.sstewartgallus.plato.ir.type.Type;
import com.sstewartgallus.plato.java.IntF;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleInfo;

public final class JitStatic<A> implements U<A> {
    private final MethodHandle methodHandle;
    private final MethodHandleInfo info;
    private final Type<A> type;

    public JitStatic(MethodHandleInfo info,
                     Type<A> type,
                     MethodHandle methodHandle) {
        this.info = info;
        this.type = type;
        this.methodHandle = methodHandle;
    }

    private static U<?> box(Object x) {
        if (x instanceof U<?> thunk) {
            return thunk;
        }

        return IntF.of((int) x);
    }

    @Override
    public String toString() {
        return info.toString();
    }

}

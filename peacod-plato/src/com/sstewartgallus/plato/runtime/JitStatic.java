package com.sstewartgallus.plato.runtime;

import com.sstewartgallus.plato.ir.type.Type;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleInfo;

final class JitStatic<A> implements U<A> {
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

    MethodHandle methodHandle() {
        return methodHandle;
    }

    @Override
    public String toString() {
        return info.toString();
    }
}

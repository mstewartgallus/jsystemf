package com.sstewartgallus.plato.runtime;

import com.sstewartgallus.plato.runtime.type.Stk;
import com.sstewartgallus.plato.runtime.type.Type;
import com.sstewartgallus.plato.runtime.type.U;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleInfo;

public final class JitStatic<A> extends U<A> {
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


    @Override
    public String toString() {
        return info.toString();
    }

    @Override
    public <C> void enter(Continuation<C> context, Stk<A> action) {
        throw null;
    }
}

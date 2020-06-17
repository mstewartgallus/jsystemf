package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.Jit;
import com.sstewartgallus.plato.runtime.U;

import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;

public interface Code<A> {
    TypeDesc<A> type();

    default U<A> compile(MethodHandles.Lookup lookup, PrintWriter writer) {
        return Jit.jit(this, lookup, writer);
    }

    default void compile(Jit.Environment environment) {
        throw new UnsupportedOperationException(getClass().toString());
    }
}


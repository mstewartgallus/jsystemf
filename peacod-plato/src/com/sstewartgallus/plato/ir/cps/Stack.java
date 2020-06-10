package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.Jit;
import com.sstewartgallus.plato.runtime.U;

import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;

public interface Stack<A, B> {
    default Instr<B> apply(Instr<A> x) {
        return new CallStackInstr<>(x, this);
    }

    default U<B> interpret(CpsEnvironment environment, U<A> x) {
        throw new UnsupportedOperationException(getClass().toString());
    }

    default void compile(CompilerEnvironment environment) {
        throw new UnsupportedOperationException(getClass().toString());
    }

    default B compile(MethodHandles.Lookup lookup, PrintWriter writer) {
        return Jit.jit(this::compile, lookup, writer);
    }

    TypeDesc<B> range();
}

package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.U;

public record NilStack<A>() implements Stack<A, A> {
    @Override
    public Instr<A> apply(Instr<A> x) {
        return x;
    }

    @Override
    public String toString() {
        return "nil";
    }

    @Override
    public U<A> interpret(CpsEnvironment environment, U<A> x) {
        return x;
    }

    @Override
    public void compile(CompilerEnvironment environment) {
    }

    @Override
    public TypeDesc<A> range() {
        throw null;
    }
}

package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.U;

public record CallStackInstr<A, C>(Instr<A>m, Stack<A, C>k) implements Instr<C> {
    @Override
    public String toString() {
        return m + " :: " + k;
    }

    @Override
    public U<C> interpret(CpsEnvironment environment) {
        var data = m.interpret(environment);
        return k.interpret(environment, data);
    }

    @Override
    public void compile(CompilerEnvironment environment) {
        m.compile(environment);
        k.compile(environment);
    }

    @Override
    public TypeDesc<C> type() {
        return k.range();
    }
}

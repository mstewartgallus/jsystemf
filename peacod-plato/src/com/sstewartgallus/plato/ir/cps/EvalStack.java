package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.U;

public record EvalStack<A, B, C>(LocalValue<A>x,
                                 Instr<B>m,
                                 Stack<B, C>k) implements Stack<F<A>, C> {

    @Override
    public String toString() {
        return "to " + x + ". " + m + " :: " + k;
    }

    @Override
    public U<C> interpret(CpsEnvironment environment, U<F<A>> x) {
        throw null;
    }

    @Override
    public void compile(CompilerEnvironment environment) {
        throw null;
    }

    @Override
    public TypeDesc<C> range() {
        return k.range();
    }
}

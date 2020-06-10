package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.U;

import java.util.Set;

public record ForceInstr<A>(Value<U<A>>thunk) implements Instr<A> {
    @Override
    public U<A> interpret(CpsEnvironment environment) {
        return thunk.interpret(environment);
    }

    @Override
    public void compile(CompilerEnvironment environment) {
        thunk.compile(environment);
    }

    // fixme... seems wrong...
    @Override
    public Set<LocalValue<?>> dependencies() {
        return thunk.dependencies();
    }

    @Override
    public Set<LocalValue<?>> arguments() {
        return Set.of();
    }

    @Override
    public Set<LocalValue<?>> locals() {
        return Set.of();
    }

    @Override
    public TypeDesc<A> type() {
        var fType = (TypeDesc.TypeApplicationDesc<A, U<A>>) thunk.type();
        return fType.x();
    }

    @Override
    public String toString() {
        return "!" + thunk;
    }
}

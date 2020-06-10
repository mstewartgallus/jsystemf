package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.FreeImpl;
import com.sstewartgallus.plato.runtime.U;

import java.util.Set;

public record ReturnInstr<A>(Value<A>value) implements Instr<F<A>> {
    @Override
    public U<F<A>> interpret(CpsEnvironment environment) {
        A val = value.interpret(environment);
        return (FreeImpl<A>) () -> val;
    }

    @Override
    public void compile(CompilerEnvironment environment) {
        throw null;
    }

    // fixme... seems wrong...
    @Override
    public Set<LocalValue<?>> dependencies() {
        return Set.of();
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
    public TypeDesc<F<A>> type() {
        return value.type().returns();
    }

    @Override
    public String toString() {
        return "return " + value;
    }
}

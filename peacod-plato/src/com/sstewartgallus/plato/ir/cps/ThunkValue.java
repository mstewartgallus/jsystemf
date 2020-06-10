package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.U;

import java.util.Set;

public record ThunkValue<A>(Instr<A>instr) implements Value<U<A>> {

    @Override
    public U<A> interpret(CpsEnvironment environment) {
        return instr.interpret(environment);
    }

    @Override
    public Set<LocalValue<?>> dependencies() {
        return instr.dependencies();
    }

    @Override
    public TypeDesc<U<A>> type() {
        return instr.type().thunk();
    }

    @Override
    public String toString() {
        return "thunk {" + ("\n" + instr).replace("\n", "\n\t") + "\n}";
    }
}

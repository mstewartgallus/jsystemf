package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.U;

import java.util.Set;

public record LabelValue<A>(TypeDesc<A>domain, String canonicalName,
                            Set<LocalValue<?>>environment,
                            Set<LocalValue<?>>arguments) implements Value<U<A>>, Comparable<LabelValue<?>> {
    @Override
    public String toString() {
        return canonicalName + " " + environment + " " + arguments;
    }

    @Override
    public int compareTo(LabelValue<?> o) {
        return toString().compareTo(o.toString());
    }

    @Override
    public U<A> interpret(CpsEnvironment environment) {
        // fixme... grab the environment in the interpreter ?
        var instrs = environment.get(this);
        return instrs.interpret(environment);
    }

    @Override
    public void compile(CompilerEnvironment environment) {
        environment.loadLabel(this);
    }

    @Override
    public Set<LocalValue<?>> dependencies() {
        return environment;
    }

    @Override
    public TypeDesc<U<A>> type() {
        return domain.thunk();
    }
}

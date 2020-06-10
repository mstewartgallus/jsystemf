package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.type.TypeDesc;

import java.util.Set;

public record LocalValue<A>(TypeDesc<A>type, String canonicalName) implements Value<A>, Comparable<LocalValue<?>> {
    @Override
    public String toString() {
        return canonicalName;
    }

    @Override
    public int compareTo(LocalValue<?> o) {
        return toString().compareTo(o.toString());
    }

    @Override
    public A interpret(CpsEnvironment environment) {
        return environment.get(this);
    }

    @Override
    public void compile(CompilerEnvironment environment) {
        environment.loadLocal(this);
    }

    @Override
    public Set<LocalValue<?>> dependencies() {
        return Set.of(this);
    }
}

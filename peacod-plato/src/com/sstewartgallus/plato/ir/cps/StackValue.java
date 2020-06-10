package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.type.TypeDesc;

import java.util.Set;

public record StackValue<A>(TypeDesc<A>type, String canonicalName) implements Value<A>, Comparable<StackValue<?>> {
    @Override
    public String toString() {
        return canonicalName;
    }

    @Override
    public int compareTo(StackValue<?> o) {
        return toString().compareTo(o.toString());
    }

    @Override
    public A interpret(CpsEnvironment environment) {
        throw null;
    }

    @Override
    public void compile(CompilerEnvironment environment) {
        throw null;
    }

    @Override
    public Set<LocalValue<?>> dependencies() {
        return Set.of();
    }
}

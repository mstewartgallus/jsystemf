package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.type.TypeDesc;

import java.util.Set;

public record GlobalValue<A>(TypeDesc<A>type, String packageName,
                             String name) implements Value<A> {
    @Override
    public String toString() {
        return packageName + "/" + name;
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
        return Set.of();
    }
}

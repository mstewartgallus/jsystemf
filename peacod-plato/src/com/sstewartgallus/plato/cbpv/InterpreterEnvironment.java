package com.sstewartgallus.plato.cbpv;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public final class InterpreterEnvironment {
    private final Map<VarLiteral<?>, Object> map;

    public InterpreterEnvironment() {
        this.map = Map.of();
    }

    private InterpreterEnvironment(Map<VarLiteral<?>, Object> map) {
        this.map = map;
    }

    public <A> InterpreterEnvironment put(VarLiteral<A> variable, A value) {
        var copy = new TreeMap<>(map);
        copy.put(variable, value);
        return new InterpreterEnvironment(copy);
    }

    public <A> A get(VarLiteral<A> variable) {
        var result = (A) map.get(variable);
        Objects.requireNonNull(result);
        return result;
    }
}

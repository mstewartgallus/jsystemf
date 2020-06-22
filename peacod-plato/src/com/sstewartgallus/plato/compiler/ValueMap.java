package com.sstewartgallus.plato.compiler;

import com.sstewartgallus.plato.ir.Variable;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

final class ValueMap {
    private static final Object SENTINEL = new Object();
    private final Map<Variable, Object> values;

    public ValueMap() {
        this.values = Map.of();
    }

    private ValueMap(Map<Variable, Object> values) {
        this.values = Objects.requireNonNull(values);
    }

    public <A> A get(Variable<A> variable) {
        var value = values.getOrDefault(variable, SENTINEL);
        if (value == SENTINEL) {
            throw new IllegalArgumentException("Variable " + variable + " not found in " + values);
        }
        return (A) value;
    }

    public <A> ValueMap put(Variable<A> variable, A value) {
        var copy = new TreeMap<>(values);
        copy.put(variable, value);
        return new ValueMap(copy);
    }

    @Override
    public String toString() {
        return values.toString();
    }
}

package com.sstewartgallus.plato.compiler;

import com.sstewartgallus.plato.ir.Label;
import com.sstewartgallus.plato.runtime.type.Stk;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

final class ThunkMap {

    private final Map<Label, Stk> values;

    public ThunkMap() {
        this.values = Map.of();
    }

    private ThunkMap(Map<Label, Stk> values) {
        this.values = Objects.requireNonNull(values);
    }

    public <A> Stk<A> get(Label<A> variable) {
        var value = values.get(variable);
        if (value == null) {
            throw new IllegalArgumentException("Variable " + variable + " not found in " + values);
        }
        return value;
    }

    public <A> ThunkMap put(Label<A> variable, Stk<A> value) {
        var copy = new TreeMap<>(values);
        copy.put(variable, value);
        return new ThunkMap(copy);
    }

    @Override
    public String toString() {
        return values.toString();
    }
}

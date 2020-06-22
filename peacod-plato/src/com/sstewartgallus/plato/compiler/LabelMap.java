package com.sstewartgallus.plato.compiler;

import com.sstewartgallus.plato.ir.Label;
import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.cps.Value;
import com.sstewartgallus.plato.runtime.type.Stk;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

final class LabelMap {
    private final Map<Label, Value> labels;
    private final Map<Variable, Value> values;

    public LabelMap() {
        this.labels = Map.of();
        this.values = Map.of();
    }

    private LabelMap(Map<Label, Value> labels,
                     Map<Variable, Value> values) {
        this.labels = Objects.requireNonNull(labels);
        this.values = Objects.requireNonNull(values);
    }

    public <A> Value<Stk<A>> get(Label<A> label) {
        return (Value) labels.get(label);
    }

    public <A> LabelMap put(Label<A> label, Value<Stk<A>> f) {
        var copy = new TreeMap<>(labels);
        copy.put(label, f);
        return new LabelMap(copy, values);
    }

    public <B> LabelMap put(Variable<B> variable, Value<B> value) {
        var copy = new TreeMap<>(values);
        copy.put(variable, value);
        return new LabelMap(labels, copy);
    }

    @Override
    public String toString() {
        return labels.toString();
    }

    public <C> Value<C> get(Variable<C> label) {
        return values.get(label);
    }
}

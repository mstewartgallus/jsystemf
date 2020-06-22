package com.sstewartgallus.plato.compiler;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.cps.Value;

import java.util.Map;
import java.util.TreeMap;

final class KontMap {
    private final Map<Variable, Value> labels;

    public KontMap() {
        this.labels = Map.of();
    }

    private KontMap(Map<Variable, Value> labels) {
        this.labels = labels;
    }

    public <A> Value<A> get(Variable<A> label) {
        return labels.get(label);
    }

    public <A> KontMap put(Variable<A> label, Value<A> f) {
        var copy = new TreeMap<>(labels);
        copy.put(label, f);
        return new KontMap(copy);
    }
}

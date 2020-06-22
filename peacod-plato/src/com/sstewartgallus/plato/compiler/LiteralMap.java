package com.sstewartgallus.plato.compiler;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.cbpv.Literal;

import java.util.Map;
import java.util.TreeMap;

final class LiteralMap {
    private final Map<Variable, Literal> variables;

    public LiteralMap() {
        this.variables = Map.of();
    }

    private LiteralMap(Map<Variable, Literal> variables) {
        this.variables = variables;
    }

    public <A> Literal<A> get(Variable<A> variable) {
        return variables.get(variable);
    }

    public <A> LiteralMap put(Variable<A> binder, Literal<A> f) {
        var copy = new TreeMap<>(variables);
        copy.put(binder, f);
        return new LiteralMap(copy);
    }

    public <A> LiteralMap clear(Variable<A> binder) {
        var copy = new TreeMap<>(variables);
        copy.remove(binder);
        return new LiteralMap(copy);
    }
}

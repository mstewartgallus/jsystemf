package com.sstewartgallus.plato.syntax.term;

import com.sstewartgallus.plato.cbpv.Literal;
import com.sstewartgallus.plato.runtime.U;
import com.sstewartgallus.plato.syntax.ext.variables.VarTerm;
import com.sstewartgallus.plato.cbpv.Code;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public final class Environment {
    private final Map<VarTerm<?>, Literal<?>> map;

    public Environment() {
        this.map = Map.of();
    }

    private Environment(Map<VarTerm<?>, Literal<?>> map) {
        this.map = map;
    }

    public <A> Environment put(VarTerm<A> variable, Literal<U<A>> value) {
        var copy = new TreeMap<>(map);
        copy.put(variable, value);
        return new Environment(copy);
    }

    public <A> Literal<U<A>> get(VarTerm<A> variable) {
        var result = (Literal<U<A>>)map.get(variable);
        Objects.requireNonNull(result, "variable " + variable + " " + map);
        return result;
    }
}

package com.sstewartgallus.plato.compiler;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.systemf.Term;
import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.type.Stk;

import java.util.Map;
import java.util.TreeMap;

final class TermMap {
    private final Map<Variable, Term> variables;

    public TermMap() {
        this.variables = Map.of();
    }

    private TermMap(Map<Variable, Term> variables) {
        this.variables = variables;
    }

    public <A> TermMap clear(Variable<A> binder) {
        var copy = new TreeMap<>(variables);
        copy.remove(binder);
        return new TermMap(copy);
    }

    public <A> Term<A> get(Variable<Stk<F<Stk<A>>>> variable) {
        return variables.get(variable);
    }

    public <A> TermMap put(Variable<Stk<F<Stk<A>>>> binder, Term<A> f) {
        var copy = new TreeMap<>(variables);
        copy.put(binder, f);
        return new TermMap(copy);
    }
}

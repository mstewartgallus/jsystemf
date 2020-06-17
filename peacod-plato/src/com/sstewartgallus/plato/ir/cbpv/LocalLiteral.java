package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.systemf.Variable;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.Jit;

import java.util.Objects;

public record LocalLiteral<A>(Variable<A>variable) implements Literal<A> {
    public LocalLiteral {
        Objects.requireNonNull(variable);
    }

    @Override
    public String toString() {
        return variable.toString();
    }

    @Override
    public TypeDesc<A> type() {
        return variable.type();
    }

    @Override
    public void compile(Jit.Environment environment) {
        environment.load(variable);
    }
}

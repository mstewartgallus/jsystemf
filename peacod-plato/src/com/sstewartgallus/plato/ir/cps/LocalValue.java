package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.Variable;

public record LocalValue<A>(Variable<A>variable) implements Value<A> {
    @Override
    public String toString() {
        return variable.toString();
    }

    @Override
    public Value<A> visitChildren(ActionVisitor actionVisitor, ValueVisitor valueVisitor) {
        return this;
    }
}

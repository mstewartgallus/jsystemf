package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.Global;

import java.util.Objects;

public record GlobalValue<A>(Global<A>global) implements Value<A> {
    public GlobalValue {
        Objects.requireNonNull(global);
    }

    @Override
    public String toString() {
        return global.toString();
    }

    @Override
    public Value<A> visitChildren(ActionVisitor actionVisitor, ValueVisitor valueVisitor) {
        return this;
    }
}

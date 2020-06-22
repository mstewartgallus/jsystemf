package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.runtime.F;

public record ReturnAction<A>(Value<A>value) implements Action<F<A>> {
    @Override
    public String toString() {
        return "return " + value;
    }

    @Override
    public Action<F<A>> visitChildren(ActionVisitor actionVisitor, ValueVisitor valueVisitor) {
        return new ReturnAction<>(valueVisitor.onValue(value));
    }
}

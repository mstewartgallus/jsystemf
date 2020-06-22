package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.Label;
import com.sstewartgallus.plato.runtime.type.Stk;

public record StackLabelValue<A>(Label<A>label) implements Value<Stk<A>> {
    @Override
    public String toString() {
        return label.toString();
    }

    @Override
    public Value<Stk<A>> visitChildren(ActionVisitor actionVisitor, ValueVisitor valueVisitor) {
        return this;
    }
}

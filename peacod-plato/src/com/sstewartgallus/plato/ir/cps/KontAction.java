package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.Label;
import com.sstewartgallus.plato.runtime.type.Behaviour;

public record KontAction<A>(Label<A>label, Action<Behaviour>action) implements Action<A> {
    @Override
    public String toString() {
        return "κ " + label + " →\n" + action;
    }

    @Override
    public Action<A> visitChildren(ActionVisitor actionVisitor, ValueVisitor valueVisitor) {
        return new KontAction<>(label, actionVisitor.onAction(action));
    }
}
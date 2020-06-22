package com.sstewartgallus.plato.ir.cps;


import com.sstewartgallus.plato.runtime.type.Behaviour;
import com.sstewartgallus.plato.runtime.type.Stk;

import java.util.Objects;

public record ApplyStackAction<A>(Value<Stk<A>>next, Action<A>action) implements Action<Behaviour> {
    public ApplyStackAction {
        Objects.requireNonNull(action);
        Objects.requireNonNull(next);
    }

    @Override
    public String toString() {
        return "" + action + "\n-----------\n" + next + "";
    }

    @Override
    public Action<Behaviour> visitChildren(ActionVisitor actionVisitor, ValueVisitor valueVisitor) {
        return new ApplyStackAction<>(valueVisitor.onValue(next), actionVisitor.onAction(action));
    }
}


package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.type.Behaviour;
import com.sstewartgallus.plato.runtime.type.Stk;

public record SimpleLambdaValue<A>(Variable<A>label, Action<Behaviour>action) implements Value<Stk<F<A>>> {
    @Override
    public String toString() {
        return "κ " + label + " →\n" + action;
    }

    @Override
    public Value<Stk<F<A>>> visitChildren(ActionVisitor actionVisitor, ValueVisitor valueVisitor) {
        return new SimpleLambdaValue<>(label, actionVisitor.onAction(action));
    }
}
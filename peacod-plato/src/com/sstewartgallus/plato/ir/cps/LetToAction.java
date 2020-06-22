package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.runtime.F;

import java.util.Objects;

public record LetToAction<A, B>(Variable<A>binder, Action<F<A>>action, Action<B>body) implements Action<B> {
    public LetToAction {
        Objects.requireNonNull(binder);
        Objects.requireNonNull(action);
        Objects.requireNonNull(body);
    }

    @Override
    public Action<B> visitChildren(ActionVisitor actionVisitor, ValueVisitor valueVisitor) {
        return new LetToAction<>(binder, actionVisitor.onAction(action), actionVisitor.onAction(body));
    }

    @Override
    public String toString() {
        return action + " to " + binder + ".\n" + body;
    }
}
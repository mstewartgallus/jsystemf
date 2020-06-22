package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.runtime.Fn;

import java.util.Objects;

public record ApplyAction<A, B>(Action<Fn<A, B>>f, Value<A>x) implements Action<B> {
    public ApplyAction {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    @Override
    public String toString() {
        return x + "\n" + f;
    }

    @Override
    public Action<B> visitChildren(ActionVisitor actionVisitor, ValueVisitor valueVisitor) {
        return new ApplyAction<>(actionVisitor.onAction(f), valueVisitor.onValue(x));
    }
}

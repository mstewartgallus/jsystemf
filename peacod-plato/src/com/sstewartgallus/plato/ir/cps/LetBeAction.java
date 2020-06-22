package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.Variable;

import java.util.Objects;

public record LetBeAction<A, B>(Variable<A>binder, Value<A>value, Action<B>body) implements Action<B> {
    public LetBeAction {
        Objects.requireNonNull(binder);
        Objects.requireNonNull(value);
        Objects.requireNonNull(body);
    }

    @Override
    public String toString() {
        return value + " be " + binder + ".\n" + body;
    }

    @Override
    public Action<B> visitChildren(ActionVisitor actionVisitor, ValueVisitor valueVisitor) {
        return new LetBeAction<>(binder, valueVisitor.onValue(value), actionVisitor.onAction(body));
    }
}
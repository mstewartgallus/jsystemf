package com.sstewartgallus.plato.ir.cps;

public interface Action<A> {
    default Action<A> visitChildren(ActionVisitor actionVisitor, ValueVisitor valueVisitor) {
        throw new UnsupportedOperationException(getClass().toString());
    }
}

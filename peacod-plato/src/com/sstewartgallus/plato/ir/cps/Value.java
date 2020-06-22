package com.sstewartgallus.plato.ir.cps;

public interface Value<A> {
    default Value<A> visitChildren(ActionVisitor actionVisitor, ValueVisitor valueVisitor) {
        throw new UnsupportedOperationException(getClass().toString());
    }
}

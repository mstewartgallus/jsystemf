package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.Constant;

import java.util.Objects;

public record ConstantValue<A>(Constant<A>constant) implements Value<A> {
    public ConstantValue {
        Objects.requireNonNull(constant);
    }

    @Override
    public String toString() {
        return constant.toString();
    }

    @Override
    public Value<A> visitChildren(ActionVisitor actionVisitor, ValueVisitor valueVisitor) {
        return this;
    }
}

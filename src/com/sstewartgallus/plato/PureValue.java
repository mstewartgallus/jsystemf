package com.sstewartgallus.plato;

import java.lang.constant.ConstantDesc;
import java.util.Objects;

// fixme.. can't be constantdesc..., just embed a value...
public record PureValue<A>(Type<A>type, ConstantDesc value) implements ValueTerm<A> {
    public PureValue {
        Objects.requireNonNull(type);
        Objects.requireNonNull(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public <X> X visit(Visitor<X, A> visitor) {
        return visitor.onPure(type, value);
    }
}

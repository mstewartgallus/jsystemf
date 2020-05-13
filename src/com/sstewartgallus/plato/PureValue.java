package com.sstewartgallus.plato;

import java.util.Objects;

public record PureValue<A>(Type<A>type, A value) implements ValueTerm<A>, CoreTerm<A> {
    public PureValue {
        Objects.requireNonNull(type);
        Objects.requireNonNull(value);
    }

    @Override
    public String toString() {
        return Objects.toString(value);
    }

}

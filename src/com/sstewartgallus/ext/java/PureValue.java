package com.sstewartgallus.ext.java;

import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.ValueTerm;

import java.util.Objects;

public record PureValue<A>(Type<A>type, A value) implements ValueTerm<A> {
    public PureValue {
        Objects.requireNonNull(type);
        Objects.requireNonNull(value);
    }

    @Override
    public String toString() {
        return Objects.toString(value);
    }
}

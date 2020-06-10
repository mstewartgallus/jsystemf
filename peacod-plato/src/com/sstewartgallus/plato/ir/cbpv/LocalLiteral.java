package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.cps.LocalValue;
import com.sstewartgallus.plato.ir.type.TypeDesc;

import java.util.Objects;

public record LocalLiteral<A>(TypeDesc<A>type, String name) implements Literal<A> {
    public LocalLiteral {
        Objects.requireNonNull(type);
        Objects.requireNonNull(name);
    }

    @Override
    public String toString() {
        return name;
    }

    public LocalValue<A> toValue() {
        return new LocalValue<>(type, name);
    }

}

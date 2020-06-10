package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.type.TypeDesc;

import java.util.Objects;

public record LetBeCode<A, B>(LocalLiteral<A>binder, Literal<A>value, Code<B>body) implements Code<B> {
    public LetBeCode {
        Objects.requireNonNull(binder);
        Objects.requireNonNull(value);
        Objects.requireNonNull(body);
    }

    @Override
    public TypeDesc<B> type() {
        return body.type();
    }

    @Override
    public String toString() {
        return value + " be " + binder + " ∈ " + binder.type() + ".\n" + body;
    }
}
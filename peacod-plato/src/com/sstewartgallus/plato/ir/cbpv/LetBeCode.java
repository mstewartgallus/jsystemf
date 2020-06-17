package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.systemf.Variable;
import com.sstewartgallus.plato.ir.type.TypeDesc;

import java.util.Objects;

public record LetBeCode<A, B>(Variable<A>binder, Literal<A>value, Code<B>body) implements Code<B> {
    public LetBeCode {
        Objects.requireNonNull(binder);
        Objects.requireNonNull(value);
        Objects.requireNonNull(body);
    }

    public static <A, C> Code<A> of(Variable<C> binder, Literal<C> value, Code<A> body) {
        return new LetBeCode<>(binder, value, body);
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
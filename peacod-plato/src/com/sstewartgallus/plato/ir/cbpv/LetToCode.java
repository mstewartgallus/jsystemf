package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.F;

import java.util.Objects;

public record LetToCode<A, B>(LocalLiteral<A>binder, Code<F<A>>action, Code<B>body) implements Code<B> {
    public LetToCode {
        Objects.requireNonNull(binder);
        Objects.requireNonNull(action);
        Objects.requireNonNull(body);
    }

    @Override
    public TypeDesc<B> type() {
        return body.type();
    }

    @Override
    public String toString() {
        return action + " to " + binder + " ∈ " + binder.type() + ".\n" + body;
    }
}
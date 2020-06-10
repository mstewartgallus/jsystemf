package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.Fn;

import java.util.Objects;


public record LambdaCode<A, B>(LocalLiteral<A>binder, Code<B>body) implements Code<Fn<A, B>> {
    public LambdaCode {
        Objects.requireNonNull(binder);
        Objects.requireNonNull(body);
    }

    @Override
    public final TypeDesc<Fn<A, B>> type() {
        return binder.type().toFn(body.type());
    }

    @Override
    public final String toString() {
        return "λ " + binder.name() + " ∈ " + binder.type() + " →\n" + body;
    }
}

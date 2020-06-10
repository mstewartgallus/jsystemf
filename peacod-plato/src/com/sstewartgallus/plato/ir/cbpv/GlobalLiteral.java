package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.type.TypeDesc;

import java.util.Objects;

public record GlobalLiteral<A>(TypeDesc<A>type, String packageName, String name) implements Literal<A> {
    public GlobalLiteral {
        Objects.requireNonNull(type);
        Objects.requireNonNull(packageName);
        Objects.requireNonNull(name);
    }

    @Override
    public String toString() {
        return packageName + "/" + name;
    }

}

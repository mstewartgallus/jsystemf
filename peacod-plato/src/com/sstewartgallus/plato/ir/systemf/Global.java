package com.sstewartgallus.plato.ir.systemf;

import com.sstewartgallus.plato.ir.type.TypeDesc;

import java.util.Objects;

public record Global<A>(TypeDesc<A>type, String packageName, String name) {
    public Global {
        Objects.requireNonNull(type);
        Objects.requireNonNull(packageName);
        Objects.requireNonNull(name);
    }

    @Override
    public String toString() {
        return packageName + "/" + name;
    }
}

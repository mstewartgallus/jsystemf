package com.sstewartgallus.plato.runtime;

import java.lang.invoke.MethodHandle;
import java.util.Objects;

public record Label<A>(MethodHandle handle) {
    public Label {
        Objects.requireNonNull(handle);
    }
}

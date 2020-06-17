package com.sstewartgallus.plato.runtime;

import java.util.Arrays;
import java.util.Objects;

// fixme... metadata..
public record Env<A>(Object[]values) {
    public Env {
        Objects.requireNonNull(values);
    }

    @Override
    public String toString() {
        return Arrays.toString(values);
    }
}

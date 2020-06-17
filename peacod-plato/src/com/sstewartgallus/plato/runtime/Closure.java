package com.sstewartgallus.plato.runtime;

import java.util.Arrays;

public record Closure<A>(Object mylabel, Object[]environment) implements U<A> {
    public String toString() {
        return "<" + mylabel + ", " + Arrays.toString(environment) + ">";
    }

}

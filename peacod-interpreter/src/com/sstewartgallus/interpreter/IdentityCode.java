package com.sstewartgallus.interpreter;

import java.util.function.Function;

public record IdentityCode<A>() implements Code<Function<A, A>> {

    @Override
    public String toString() {
        return "I";
    }
}

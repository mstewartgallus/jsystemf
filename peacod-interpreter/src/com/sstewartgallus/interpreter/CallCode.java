package com.sstewartgallus.interpreter;

import java.util.function.Function;

public record CallCode<Z, A, B>() implements Code<Function<Function<Z, Function<A, B>>, Function<Function<Z, A>, Function<Z, B>>>> {
    @Override
    public String toString() {
        return "S";
    }

    private static <B, A, Z> Function<Function<Z, A>, Function<Z, B>> callCode(Function<Z, Function<A, B>> f) {
        return x -> z -> f.apply(z).apply(x.apply(z));
    }

}

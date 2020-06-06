package com.sstewartgallus.interpreter;

import java.util.function.Function;

public record ConstantCode<A, B>() implements Code<Function<A, Function<B, A>>> {
    @Override
    public String toString() {
        return "K";
    }

}

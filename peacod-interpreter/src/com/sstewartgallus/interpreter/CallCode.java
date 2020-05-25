package com.sstewartgallus.interpreter;

import java.util.function.Function;

public record CallCode<Z, A, B>() implements Code<Function<Function<Z, Function<A, B>>, Function<Function<Z, A>, Function<Z, B>>>> {
    @Override
    public String toString() {
        return "S";
    }

    @Override
    public <X> Interpreter<?, X> execute(Interpreter<Function<Function<Z, Function<A, B>>, Function<Function<Z, A>, Function<Z, B>>>, X> interpreter) {
        return interpreter.pure(CallCode::callCode);
    }

    private static <B, A, Z> Function<Function<Z, A>, Function<Z, B>> callCode(Function<Z, Function<A, B>> f) {
        return x -> z -> f.apply(z).apply(x.apply(z));
    }

    @Override
    public <X> Code<Function<X, Function<Function<Z, Function<A, B>>, Function<Function<Z, A>, Function<Z, B>>>>> pointFree(Id<X> v) {
        return new ApplyCode<>(new ConstantCode<>(), this);
    }
}

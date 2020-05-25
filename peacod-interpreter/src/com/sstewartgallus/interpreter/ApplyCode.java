package com.sstewartgallus.interpreter;

import java.util.Objects;
import java.util.function.Function;

public record ApplyCode<A, B>(Code<Function<A, B>>f, Code<A>x) implements Code<B> {
    public ApplyCode {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    @Override
    public String toString() {
        return "(" + f + " " + x + ")";
    }

    @Override
    public <X> Interpreter<?, X> execute(Interpreter<B, X> interpreter) {
        return interpreter.apply(f, x);
    }

    @Override
    public <X> Code<Function<X, B>> pointFree(Id<X> v) {
        var fC = f.pointFree(v);
        var xC = x.pointFree(v);
        return new ApplyCode<>(new ApplyCode<>(new CallCode<>(), fC), xC);
    }
}

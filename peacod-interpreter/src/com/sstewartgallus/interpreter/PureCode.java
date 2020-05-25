package com.sstewartgallus.interpreter;

import java.util.function.Function;

public record PureCode<A>(A value) implements Code<A> {
    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public <X> Interpreter<?, X> execute(Interpreter<A, X> interpreter) {
        return interpreter.pure(value);
    }

    @Override
    public <X> Code<Function<X, A>> pointFree(Id<X> v) {
        return new ApplyCode<>(new ConstantCode<>(), this);
    }
}

package com.sstewartgallus.interpreter;

import java.util.function.Function;

public record IdentityCode<A>() implements Code<Function<A, A>> {
    @Override
    public <X> Interpreter<?, X> execute(Interpreter<Function<A, A>, X> interpreter) {
        return interpreter.pure(x -> x);
    }

    @Override
    public <X> Code<Function<X, Function<A, A>>> pointFree(Id<X> v) {
        return new ApplyCode<>(new ConstantCode<>(), this);
    }

    @Override
    public String toString() {
        return "I";
    }
}

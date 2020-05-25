package com.sstewartgallus.interpreter;

import java.util.Objects;
import java.util.function.Function;

public final class LoadCode<A> implements Code<A> {
    public final Id<A> variable;

    public LoadCode(Id<A> id) {
        variable = id;
    }

    @Override
    public String toString() {
        return Objects.toString(variable);
    }

    @Override
    public <X> Interpreter<?, X> execute(Interpreter<A, X> interpreter) {
        throw new Error("temporary");
    }

    @Override
    public <X> Code<Function<X, A>> pointFree(Id<X> v) {
        if (v == variable) {
            return (Code)new IdentityCode<>();
        }
        return new ApplyCode<>(new ConstantCode<>(), this);
    }
}

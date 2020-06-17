package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.systemf.Variable;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.Jit;

import java.util.Objects;

public record LetToCode<A, B>(Variable<A>binder, Code<F<A>>action, Code<B>body) implements Code<B> {
    public LetToCode {
        Objects.requireNonNull(binder);
        Objects.requireNonNull(action);
        Objects.requireNonNull(body);
    }

    public static <A, B> Code<A> of(Variable<B> binder, Code<F<B>> action, Code<A> body) {
        if (action instanceof ReturnCode<B> returnCode) {
            return new LetBeCode<>(binder, returnCode.literal(), body);
        }
        return new LetToCode<>(binder, action, body);
    }

    @Override
    public void compile(Jit.Environment environment) {
        action.compile(environment);
        environment.store(binder);
        body.compile(environment);
    }

    @Override
    public TypeDesc<B> type() {
        return body.type();
    }

    @Override
    public String toString() {
        return action + " to " + binder + " ∈ " + binder.type() + ".\n" + body;
    }
}
package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.systemf.Variable;
import com.sstewartgallus.plato.runtime.Fn;

public record LambdaAction<A, B>(Variable<A>variable, Action<B>body) implements Action<Fn<A, B>> {
    public static <A, B> Action<Fn<A, B>> of(Variable<A> variable, Action<B> body) {
        return new LambdaAction<>(variable, body);
    }

    public String toString() {
        return "λ " + variable + " → " + body;
    }
}

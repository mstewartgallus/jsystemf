package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.runtime.Fn;

public record LambdaAction<A, B>(Variable<A>variable, Action<B>body) implements Action<Fn<A, B>> {
    public String toString() {
        return "λ " + variable + " →\n" + body;
    }
}

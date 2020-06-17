package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.systemf.Variable;
import com.sstewartgallus.plato.runtime.F;

public record LetToKont<A>(Variable<A>variable, Instr body) implements Kont<F<A>> {
    public static <A> Kont<F<A>> of(Variable<A> variable, Instr next) {
        return new LetToKont<>(variable, next);
    }

    @Override
    public String toString() {
        return "to " + variable + ".\n" + body;
    }

}

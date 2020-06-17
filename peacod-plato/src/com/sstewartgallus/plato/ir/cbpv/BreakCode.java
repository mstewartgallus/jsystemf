package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.cps.Lbl;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.Jit;

public record BreakCode<A, B>(Lbl<A>label, Code<A>argument) implements Code<B> {
    @Override
    public String toString() {
        return argument + "\n" + label;
    }

    @Override
    public TypeDesc<B> type() {
        throw null;
    }

    @Override
    public void compile(Jit.Environment environment) {
        argument.compile(environment);
        environment.jump(label);
    }
}

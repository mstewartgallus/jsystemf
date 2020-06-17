package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.systemf.Global;

public record GlobalAction<A>(Global<A>global) implements Action<A> {
    @Override
    public String toString() {
        return global.toString();
    }

}

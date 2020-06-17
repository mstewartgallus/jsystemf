package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.systemf.Variable;

public record LocalValue<A>(Variable<A>variable) implements Value<A> {
    @Override
    public String toString() {
        return variable.toString();
    }

}

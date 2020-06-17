package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.runtime.F;

public record ReturnAction<A>(Value<A>value) implements Action<F<A>> {
    @Override
    public String toString() {
        return "return " + value;
    }


}

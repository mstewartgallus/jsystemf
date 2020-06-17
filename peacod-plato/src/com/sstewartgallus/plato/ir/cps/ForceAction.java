package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.runtime.U;

public record ForceAction<A>(Value<U<A>>thunk) implements Action<A> {
    @Override
    public String toString() {
        return "! " + thunk;
    }

}

package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.runtime.type.U;

public record FixPointKont<A>(Variable<U<A>>variable, Action<A>action) implements Action<A> {
    @Override
    public String toString() {
        return "fix " + variable + ".\n" + action;
    }

}

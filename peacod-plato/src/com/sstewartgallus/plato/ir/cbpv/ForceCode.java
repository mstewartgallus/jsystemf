package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.U;

public record ForceCode<A>(Literal<U<A>>thunk) implements Code<A> {
    @Override
    public TypeDesc<A> type() {
        var fType = (TypeDesc.TypeApplicationDesc<A, U<A>>) thunk.type();
        return fType.x();
    }

    @Override
    public String toString() {
        return "!" + thunk;
    }
}
package com.sstewartgallus.plato.ir.systemf;

import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.U;

public record LocalTerm<A>(Variable<U<A>>variable) implements Term<A> {

    @Override
    public String toString() {
        return variable.toString();
    }

    @Override
    public TypeDesc<A> type() {
        var fType = (TypeDesc.TypeApplicationDesc<A, U<A>>) variable.type();
        return fType.x();
    }
}

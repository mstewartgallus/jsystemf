package com.sstewartgallus.plato.ir.systemf;

import com.sstewartgallus.plato.ir.type.TypeDesc;

public record LocalTerm<A>(TypeDesc<A>type, String name) implements VariableTerm<A> {

    @Override
    public String toString() {
        return name;
    }
}

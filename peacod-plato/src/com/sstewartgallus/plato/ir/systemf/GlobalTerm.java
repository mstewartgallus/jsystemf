package com.sstewartgallus.plato.ir.systemf;

import com.sstewartgallus.plato.ir.type.TypeDesc;

public record GlobalTerm<A>(TypeDesc<A>type, String packageName, String name) implements VariableTerm<A> {
    @Override
    public String toString() {
        return packageName + "/" + name;
    }
}

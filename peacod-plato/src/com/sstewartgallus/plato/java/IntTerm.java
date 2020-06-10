package com.sstewartgallus.plato.java;

import com.sstewartgallus.plato.ir.systemf.Term;
import com.sstewartgallus.plato.ir.type.TypeDesc;

public record IntTerm(int value) implements Term<IntF> {
    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public TypeDesc<IntF> type() {
        return TypeDesc.ofReference("core", "int");
    }

}

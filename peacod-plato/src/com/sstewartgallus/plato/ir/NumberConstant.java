package com.sstewartgallus.plato.ir;

import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.java.IntF;

import java.math.BigInteger;
import java.util.Objects;

// fixme... don't fix the interpretation, and use biginteger or bigdecimal
public record NumberConstant(BigInteger value) implements Constant<IntF> {
    public NumberConstant {
        Objects.requireNonNull(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public TypeDesc<IntF> type() {
        return TypeDesc.ofReference("core", "int");
    }

}

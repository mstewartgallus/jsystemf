package com.sstewartgallus.plato.java;

import com.sstewartgallus.plato.syntax.type.NominalType;

public class IntType extends NominalType<Integer> {
    public static final IntType INT_TYPE = new IntType();

    private IntType() {
        super(new PrimTag<>(int.class));
    }
}

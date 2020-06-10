package com.sstewartgallus.plato.ir.type;

import com.sstewartgallus.plato.java.IntF;

final class IntFType extends NamedType<IntF> {
    public static final IntFType INTF_TYPE = new IntFType();

    private IntFType() {
        super("core", "int");
    }
}
package com.sstewartgallus.plato.java;

import com.sstewartgallus.plato.ir.type.TypeDesc;

public class IntType {
    public static final TypeDesc<Integer> INT_TYPE = TypeDesc.ofReference("core", "#int");
    public static final TypeDesc<IntF> INTF_TYPE = TypeDesc.ofReference("core", "int");
}

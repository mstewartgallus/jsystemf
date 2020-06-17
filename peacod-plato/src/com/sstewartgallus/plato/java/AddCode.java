package com.sstewartgallus.plato.java;

import com.sstewartgallus.plato.ir.cbpv.Code;
import com.sstewartgallus.plato.ir.cbpv.Literal;
import com.sstewartgallus.plato.ir.type.TypeDesc;

public record AddCode(Literal<Integer>left, Literal<Integer>right) implements Code<IntF> {
    @Override
    public String toString() {
        return left + "\n" + right + "\n+";
    }

    @Override
    public TypeDesc<IntF> type() {
        return IntType.INTF_TYPE;
    }


}
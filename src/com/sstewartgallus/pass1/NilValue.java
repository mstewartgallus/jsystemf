package com.sstewartgallus.pass1;

import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.ValueTerm;

public enum NilValue implements ValueTerm<HList.Nil> {
    NIL;

    @Override
    public Type<HList.Nil> type() {
        return NilNormal.NIL;
    }
}

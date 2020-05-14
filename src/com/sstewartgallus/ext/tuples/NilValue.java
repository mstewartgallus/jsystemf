package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.ValueTerm;

public enum NilValue implements ValueTerm<Nil> {
    NIL;

    @Override
    public Term<Nil> visitChildren(Visitor visitor) {
        return NIL;
    }

    @Override
    public Type<Nil> type() {
        return NilType.NIL;
    }
}

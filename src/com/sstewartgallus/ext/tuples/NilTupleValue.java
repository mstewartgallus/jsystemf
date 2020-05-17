package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.ValueTerm;

public enum NilTupleValue implements ValueTerm<N> {
    NIL;

    @Override
    public Term<N> visitChildren(Visitor visitor) {
        return NIL;
    }

    @Override
    public Type<N> type() {
        return NilTupleType.NIL;
    }
}

package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.ThunkTerm;
import com.sstewartgallus.plato.Type;

import java.util.List;
import java.util.Objects;

public record FlatValue<T extends Tuple<T>>(Type<T>type, List<Term<?>>list) implements ThunkTerm<T> {
    public FlatValue {
        Objects.requireNonNull(list);
    }

    @Override
    public Term<T> visitChildren(Visitor visitor) {
        throw null;
    }

    @Override
    public String toString() {
        return list.toString();
    }

    @Override
    public Term<T> stepThunk() {
        throw null;
    }
}

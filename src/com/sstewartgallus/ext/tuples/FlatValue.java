package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.ThunkTerm;
import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.ValueTerm;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public record FlatValue<T extends Tuple<T>>(Type<T>type, List<Term<?>>list) implements ThunkTerm<T> {
    public FlatValue {
        Objects.requireNonNull(list);
    }

    @Override
    public <B> Term<B> stepThunk(Function<ValueTerm<T>, Term<B>> k) {
        return null;
    }

    @Override
    public Term<T> visitChildren(Visitor visitor) {
        throw null;
    }

    @Override
    public String toString() {
        return list.toString();
    }
}

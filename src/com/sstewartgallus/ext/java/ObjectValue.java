package com.sstewartgallus.ext.java;

import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.TypeCheckException;
import com.sstewartgallus.plato.ValueTerm;

import java.util.Objects;

public record ObjectValue<A>(A value) implements ValueTerm<A>, JavaTerm<A> {
    public ObjectValue {
        Objects.requireNonNull(value);
    }

    @Override
    public Term<A> visitChildren(Visitor visitor) {
        return this;
    }

    @Override
    public String toString() {
        return Objects.toString(value);
    }

    @Override
    public Type<A> type() throws TypeCheckException {
        return new JavaType<A>((Class) value.getClass());
    }
}

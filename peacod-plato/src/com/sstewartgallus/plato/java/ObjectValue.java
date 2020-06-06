package com.sstewartgallus.plato.java;

import com.sstewartgallus.plato.syntax.term.Term;
import com.sstewartgallus.plato.syntax.type.NominalType;
import com.sstewartgallus.plato.syntax.type.Type;

import java.util.Objects;

public record ObjectValue<A>(A value) implements Term<J<A>>, JavaTerm<A> {
    public ObjectValue {
        Objects.requireNonNull(value);
    }

    @Override
    public Term<J<A>> visitChildren(Visitor visitor) {
        return this;
    }

    @Override
    public String toString() {
        return Objects.toString(value);
    }

    @Override
    public Type<J<A>> type() {
        return NominalType.ofTag(new JavaTag<>((Class) value.getClass()));
    }
}

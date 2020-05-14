package com.sstewartgallus.ext.java;

import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.TypeCheckException;
import com.sstewartgallus.plato.ValueTerm;

import java.util.Objects;

public record IntValue(int value) implements ValueTerm<Integer>, JavaTerm<Integer> {
    @Override
    public String toString() {
        return Objects.toString(value);
    }

    @Override
    public Type<Integer> type() throws TypeCheckException {
        return new JavaType<>(int.class);
    }

    @Override
    public Term<Integer> visitChildren(Visitor visitor) {
        return this;
    }
}

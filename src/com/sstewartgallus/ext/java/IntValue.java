package com.sstewartgallus.ext.java;

import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.TypeCheckException;
import com.sstewartgallus.plato.ValueTerm;

public record IntValue(int value) implements ValueTerm<J<Integer>>, JavaTerm<Integer> {
    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public Type<J<Integer>> type() throws TypeCheckException {
        return new JavaType<>(int.class);
    }

    @Override
    public Term<J<Integer>> visitChildren(Visitor visitor) {
        return this;
    }
}

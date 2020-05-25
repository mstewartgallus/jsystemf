package com.sstewartgallus.ext.java;

import com.sstewartgallus.plato.*;

public record IntValue(int value) implements ValueTerm<J<Integer>>, JavaTerm<Integer> {
    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public Type<J<Integer>> type() throws TypeCheckException {
        return NominalType.ofTag(new JavaTag<>(int.class));
    }

    @Override
    public Term<J<Integer>> visitChildren(Visitor visitor) {
        return this;
    }

}

package com.sstewartgallus.plato.java;

import com.sstewartgallus.plato.cbpv.Code;
import com.sstewartgallus.plato.cbpv.ReturnCode;
import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.syntax.term.Environment;
import com.sstewartgallus.plato.syntax.term.Term;
import com.sstewartgallus.plato.syntax.type.Type;

public record IntTerm(int value) implements Term<F<Integer>> {
    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public Type<F<Integer>> type() {
        return IntType.INT_TYPE.unboxed();
    }

    @Override
    public Term<F<Integer>> visitChildren(Visitor visitor) {
        return this;
    }

    @Override
    public Code<F<Integer>> compile(Environment environment) {
        return ReturnCode.of(new IntLiteral(value));
    }
}

package com.sstewartgallus.plato;

import com.sstewartgallus.interpreter.Code;

public record IntrinsicTerm<A>(Code<Term<A>>code) implements Term<A> {
    @Override
    public Type<A> type() throws TypeCheckException {
        return null;
    }

    @Override
    public Term<A> visitChildren(Visitor visitor) {
        return this;
    }

    @Override
    public Code<Term<A>> compile() {
        return code;
    }

    @Override
    public String toString() {
        return code.toString();
    }

}

package com.sstewartgallus.plato;

import java.util.Objects;

public record TypeApplyTerm<A, B>(Term<V<A, B>>f, Type<A>x) implements ThunkTerm<B> {
    public TypeApplyTerm {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    @Override
    public Term<B> visitChildren(Visitor visitor) {
        return new TypeApplyTerm<>(visitor.term(f), visitor.type(x));
    }

    @Override
    public <X> State<X> step(Interpreter<B, X> interpreter) {
        throw null;
    }

    @Override
    public Type<B> type() throws TypeCheckException {
        return ((ForallType<A, B>) f.type()).f().apply(x);
    }

}

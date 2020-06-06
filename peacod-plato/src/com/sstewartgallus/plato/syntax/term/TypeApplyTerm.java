package com.sstewartgallus.plato.syntax.term;

import com.sstewartgallus.plato.runtime.V;
import com.sstewartgallus.plato.syntax.type.ForallType;
import com.sstewartgallus.plato.syntax.type.Type;
import com.sstewartgallus.plato.syntax.type.TypeCheckException;

import java.util.Objects;

public record TypeApplyTerm<A, B>(Term<V<A, B>>f, Type<A>x) implements Term<B> {
    public TypeApplyTerm {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    @Override
    public Term<B> visitChildren(Term.Visitor visitor) {
        return new TypeApplyTerm<>(visitor.term(f), visitor.type(x));
    }

    @Override
    public Type<B> type() {
        return ((ForallType<A, B>) f.type()).f().apply(x);
    }

    @Override
    public Constraints findConstraints() throws TypeCheckException {
        throw null;
    }

}

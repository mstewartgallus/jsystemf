package com.sstewartgallus.plato;

import com.sstewartgallus.interpreter.Effect;

import java.util.Objects;

public record TypeApplyTerm<A, B>(Term<V<A, B>>f, Type<A>x) implements Term<B> {
    public TypeApplyTerm {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    @Override
    public Term<B> visitChildren(Visitor visitor) {
        return new TypeApplyTerm<>(visitor.term(f), visitor.type(x));
    }

    @Override
    public Effect<Term<B>> interpret() {
        var theX = x;
        return f.interpret().bind(fValue -> {
            var fLambda = ((TypeLambdaTerm<A, B>) fValue);
            return fLambda.apply(theX).interpret();
        });
    }

    @Override
    public Type<B> type() throws TypeCheckException {
        return ((ForallType<A, B>) f.type()).f().apply(x);
    }

}

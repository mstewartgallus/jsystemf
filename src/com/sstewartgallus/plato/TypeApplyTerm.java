package com.sstewartgallus.plato;

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
    public <X> Interpreter<?, X> step(Interpreter<B, X> interpreter) {
        var theX = x;
        return interpreter.evaluate(f, fValue -> {
            var fLambda = ((TypeLambdaTerm<A, B>) fValue);
            return fLambda.apply(theX);
        });
    }

    @Override
    public Type<B> type() throws TypeCheckException {
        return ((ForallType<A, B>) f.type()).f().apply(x);
    }

}

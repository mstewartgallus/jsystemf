package com.sstewartgallus.plato;

import java.util.Objects;

public record ApplyTerm<A, B>(Term<F<A, B>>f, Term<A>x) implements Term<B> {
    public ApplyTerm {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    @Override
    public <X> Interpreter<?, X> step(Interpreter<B, X> interpreter) {
        var theX = x;
        // fixme... make more like return interpreter.push(x).tailCall(f); ?
        return interpreter.evaluate(f, fValue -> {
            var fLambda = ((LambdaTerm<A, B>) fValue);
            return fLambda.apply(theX);
        });
    }

    @Override
    public Term<B> visitChildren(Visitor visitor) {
        return Term.apply(visitor.term(f), visitor.term(x));
    }

    @Override
    public Type<B> type() throws TypeCheckException {
        // fixme....
        var range = ((LambdaTerm<A, B>) f).range();

        f.type().unify(x.type().to(range));

        return range;
    }

}

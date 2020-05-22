package com.sstewartgallus.plato;

import com.sstewartgallus.interpreter.Effect;

import java.util.Objects;

public record ApplyTerm<A, B>(Term<F<A, B>>f, Term<A>x) implements Term<B> {
    public ApplyTerm {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    @Override
    public Effect<Term<B>> interpret() {
        var fEffects = f.interpret();
        var theX = x;
        // fixme... make more like return interpreter.push(x).tailCall(f); ?
        return fEffects.bind(fValue -> {
            var fLambda = ((LambdaTerm<A, B>) fValue);
            return fLambda.apply(theX).interpret();
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

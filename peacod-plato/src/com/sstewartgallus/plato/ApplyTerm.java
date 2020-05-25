package com.sstewartgallus.plato;

import com.sstewartgallus.interpreter.ApplyCode;
import com.sstewartgallus.interpreter.Code;

import java.util.Objects;

public record ApplyTerm<A, B>(Term<F<A, B>>f, Term<A>x) implements Term<B> {
    public ApplyTerm {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    @Override
    public Term<B> visitChildren(Visitor visitor) {
        return Term.apply(visitor.term(f), visitor.term(x));
    }

    @Override
    public Code<Term<B>> compile() {
        var fC = f.compile();
        var xC = x.compile();
        return new ApplyCode<>(new ApplyCode<>(new EvalCode<>(), fC), xC);
    }

    @Override
    public Type<B> type() throws TypeCheckException {
        // fixme....
        var range = ((LambdaTerm<A, B>) f).range();

        f.type().unify(x.type().to(range));

        return range;
    }

    public String toString() {
        return "(" + f + " " + x + ")";
    }
}

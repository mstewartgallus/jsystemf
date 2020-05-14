package com.sstewartgallus.ext.pointfree;

import com.sstewartgallus.plato.*;

public record Compose<A, B, C>(Term<F<A, B>>f, Term<F<B, C>>g) implements ThunkTerm<F<A, C>> {
    @Override
    public Term<F<A, C>> stepThunk() {
        var fType = (FunctionType<A, B>) f.type();
        return new LambdaValue<>(fType.domain(), x -> Term.apply(g, Term.apply(f, x)));
    }

    @Override
    public Type<F<A, C>> type() throws TypeCheckException {
        var fType = (FunctionType<A, B>) f.type();
        var gType = (FunctionType<B, C>) g.type();
        return fType.domain().to(gType.range());
    }

    @Override
    public Term<F<A, C>> visitChildren(Visitor visitor) {
        return new Compose<>(visitor.term(f), visitor.term(g));
    }
}

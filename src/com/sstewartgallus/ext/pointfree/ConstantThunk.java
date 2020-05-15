package com.sstewartgallus.ext.pointfree;

import com.sstewartgallus.plato.*;

public record ConstantThunk<A, B>(Type<A>domain, Term<B>result) implements ThunkTerm<F<A, B>> {
    @Override
    public Term<F<A, B>> stepThunk() {
        return new LambdaValue<>(domain, x -> result);
    }

    @Override
    public Type<F<A, B>> type() throws TypeCheckException {
        return domain.to(result.type());
    }

    @Override
    public Term<F<A, B>> visitChildren(Visitor visitor) {
        return new ConstantThunk<>(visitor.type(domain), visitor.term(result));
    }

    @Override
    public String toString() {
        return "(K " + result + ")";
    }
}

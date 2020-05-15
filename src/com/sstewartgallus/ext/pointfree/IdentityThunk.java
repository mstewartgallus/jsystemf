package com.sstewartgallus.ext.pointfree;

import com.sstewartgallus.plato.*;

public record IdentityThunk<A>(Type<A>domain) implements ThunkTerm<F<A, A>> {
    @Override
    public Term<F<A, A>> stepThunk() {
        return new LambdaValue<>(domain, x -> x);
    }

    @Override
    public Type<F<A, A>> type() throws TypeCheckException {
        return domain.to(domain);
    }

    @Override
    public Term<F<A, A>> visitChildren(Visitor visitor) {
        return new IdentityThunk<>(visitor.type(domain));
    }

    @Override
    public String toString() {
        return "I";
    }
}

package com.sstewartgallus.ext.pointfree;

import com.sstewartgallus.plato.*;

// fixme... try redoing as
// V<A, F<A, A>>
public record IdentityThunk<A>() implements ThunkTerm<V<A, F<A, A>>> {
    @Override
    public Term<V<A, F<A, A>>> stepThunk() {
        return Term.v(d -> d.l(x -> x));
    }

    @Override
    public Type<V<A, F<A, A>>> type() throws TypeCheckException {
        return Type.v(d -> d.to(d));
    }

    @Override
    public Term<V<A, F<A, A>>> visitChildren(Visitor visitor) {
        return this;
    }

    @Override
    public String toString() {
        return "I";
    }
}

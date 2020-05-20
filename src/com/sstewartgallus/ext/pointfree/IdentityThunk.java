package com.sstewartgallus.ext.pointfree;

import com.sstewartgallus.plato.*;

import java.util.function.Function;

// fixme... try redoing as
// V<A, F<A, A>>
public record IdentityThunk<A>() implements ThunkTerm<V<A, F<A, A>>> {
    @Override
    public <C> Term<C> stepThunk(Function<ValueTerm<V<A, F<A, A>>>, Term<C>> k) {
        return k.apply(Term.v(d -> d.l(x -> x)));
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

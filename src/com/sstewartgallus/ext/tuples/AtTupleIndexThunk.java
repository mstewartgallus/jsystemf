package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.ext.variables.VarValue;
import com.sstewartgallus.plato.*;

import java.util.Objects;

public record AtTupleIndexThunk<B extends Tuple<B>, X extends Tuple<X>, A>(Type<A>head, Type<B>tail,
                                                                           TupleIndex<X, P<A, B>>index) implements ThunkTerm<F<X, A>> {
    public AtTupleIndexThunk {
        Objects.requireNonNull(head);
        Objects.requireNonNull(tail);
    }

    @Override
    public Term<F<X, A>> visitChildren(Visitor visitor) {
        return this;
    }

    @Override
    public <Z> Term<F<Z, F<X, A>>> pointFree(VarValue<Z> varValue) {
        return Term.constant(varValue.type(), this);
    }

    @Override
    public Type<F<X, A>> type() throws TypeCheckException {
        return index.domain().to(head);
    }

    @Override
    public Term<F<X, A>> stepThunk() {
        var reify = index.reify();
        return index.domain().l(x -> {
            Term<?> current = x;
            for (var ii = 0; ii < reify; ++ii) {
                current = ((TuplePairValue<?, ?>) current).tail();
            }
            return ((TuplePairValue<A, ?>) current).head();
        });
    }

    @Override
    public String toString() {
        return "[" + index + "]";
    }

}

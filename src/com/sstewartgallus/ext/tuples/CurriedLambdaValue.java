package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.plato.*;

import java.util.Objects;

// fixme... does this make sense to have a subterm?
public record CurriedLambdaValue<L extends Tuple<L>, C, D>(Signature<L, C, D>signature,
                                                           Term<F<L, C>>f) implements ValueTerm<D> {
    public CurriedLambdaValue {
        Objects.requireNonNull(signature);
    }

    @Override
    public Term<D> visitChildren(Visitor visitor) {
        return new CurriedLambdaValue<>(signature, visitor.term(f));
    }

    @Override
    public Type<D> type() throws TypeCheckException {
        return signature.type();
    }

    @Override
    public String toString() {
        return "(curry " + f + ")";
    }
}

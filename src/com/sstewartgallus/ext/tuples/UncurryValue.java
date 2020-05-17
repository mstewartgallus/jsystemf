package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.plato.*;

import java.util.Objects;

public record UncurryValue<L extends Tuple<L>, C, D>(Signature<L, C, D>signature) implements ValueTerm<F<D, F<L, C>>> {
    public UncurryValue {
        Objects.requireNonNull(signature);
    }

    @Override
    public Term<F<D, F<L, C>>> visitChildren(Visitor visitor) {
        return this;
    }

    @Override
    public Type<F<D, F<L, C>>> type() throws TypeCheckException {
        var argType = signature.argType();
        var retType = signature.retType();
        return signature.type().to(argType.to(retType));
    }

    @Override
    public String toString() {
        return "uncurry";
    }
}

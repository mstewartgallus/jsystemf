package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.plato.*;

import java.util.Objects;

public record CurryThunk<L extends Tuple<L>, C, D>(Signature<L, C, D>signature) implements ThunkTerm<F<F<L, C>, D>> {
    public CurryThunk {
        Objects.requireNonNull(signature);
    }

    @Override
    public Term<F<F<L, C>, D>> visitChildren(Visitor visitor) {
        return this;
    }

    @Override
    public Type<F<F<L, C>, D>> type() throws TypeCheckException {
        var argType = signature.argType();
        var retType = signature.retType();
        return argType.to(retType).to(signature.type());
    }

    @Override
    public Term<F<F<L, C>, D>> stepThunk() {
        return signature.argType().to(signature.retType()).l(signature::stepThunk);
    }

    @Override
    public String toString() {
        return "curry";
    }
}

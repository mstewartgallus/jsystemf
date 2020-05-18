package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.plato.*;

import java.util.Objects;

public final class CurryValue<L extends Tuple<L>, C, D> extends LambdaValue<F<L, C>, D> {
    private final Signature<L, C, D> signature;

    public CurryValue(Signature<L, C, D> signature) {
        super(signature.argType().to(signature.retType()));
        Objects.requireNonNull(signature);
        this.signature = signature;
    }

    public Signature<L, C, D> signature() {
        return signature;
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
    public String toString() {
        return "curry";
    }

    @Override
    public Term<D> apply(Term<F<L, C>> x) {
        return new CurriedLambdaValue<>(signature, x);
    }
}

package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.plato.*;

import java.util.Objects;
import java.util.function.Function;

public final class UncurryValue<L extends Tuple<L>, C, D> implements ThunkTerm<F<D, F<L, C>>> {
    private final Signature<L, C, D> signature;

    public UncurryValue(Signature<L, C, D> signature) {
        Objects.requireNonNull(signature);
        this.signature = signature;
    }

    @Override
    public Term<F<D, F<L, C>>> visitChildren(Visitor visitor) {
        return this;
    }

    @Override
    public <B> Term<B> stepThunk(Function<ValueTerm<F<D, F<L, C>>>, Term<B>> k) {
        return k.apply(signature.type().l(x ->
                signature.argType().l(pair ->
                        pair.stepThunk(pairNorm ->
                                signature.apply(x, pairNorm)))));
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

    public Signature<L, C, D> signature() {
        return signature;
    }

}

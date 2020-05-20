package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.ext.pretty.PrettyThunk;
import com.sstewartgallus.plato.*;

import java.util.Objects;

public final class UncurryValue<L extends Tuple<L>, C, D> implements ThunkTerm<F<D, F<L, C>>> {
    private final Signature<L, C, D> signature;

    public UncurryValue(Signature<L, C, D> signature) {
        Objects.requireNonNull(signature);
        this.signature = signature;
    }

    @Override
    public Term<F<D, F<L, C>>> stepThunk() {
        return signature.type().l(x -> signature.argType().l(pair -> {
            if (pair instanceof PrettyThunk) {
                return null;
            }
            var tuple = Interpreter.normalize(pair);
            return signature.apply(x, tuple);
        }));
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

    public Signature<L, C, D> signature() {
        return signature;
    }

}

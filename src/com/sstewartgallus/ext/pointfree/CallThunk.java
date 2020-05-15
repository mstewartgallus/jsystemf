package com.sstewartgallus.ext.pointfree;

import com.sstewartgallus.plato.*;

import java.util.Objects;

public record CallThunk<Z, A, B>(Term<F<Z, F<A, B>>>f,
                                 Term<F<Z, A>>x) implements ThunkTerm<F<Z, B>>, CoreTerm<F<Z, B>> {
    public CallThunk {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    public Term<F<Z, B>> visitChildren(Visitor visitor) {
        return new CallThunk<>(visitor.term(f), visitor.term(x));
    }

    @Override
    public Type<F<Z, B>> type() throws TypeCheckException {
        var fType = f.type();

        var funType = (FunctionType<Z, F<A, B>>) fType;
        var range = funType.range();

        return funType.domain().to(((FunctionType<A, B>) range).range());
    }

    @Override
    public String toString() {
        return "(S " + f + " " + x + ")";
    }

    @Override
    public Term<F<Z, B>> stepThunk() {
        var fType = (FunctionType<Z, F<A, B>>) f.type();
        return new LambdaValue<>(fType.domain(), z -> {
            var fNorm = (LambdaValue<Z, F<A, B>>) Interpreter.normalize(f);
            var xNorm = (LambdaValue<Z, A>) Interpreter.normalize(x);
            var fValue = fNorm.apply(z);
            var xValue = xNorm.apply(z);
            var fValueNorm = (LambdaValue<A, B>) Interpreter.normalize(fValue);
            return fValueNorm.apply(xValue);
        });
    }
}

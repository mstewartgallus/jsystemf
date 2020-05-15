package com.sstewartgallus.plato;

import com.sstewartgallus.ext.variables.VarType;
import com.sstewartgallus.ir.Signature;

public record FunctionType<A, B>(Type<A>domain, Type<B>range) implements CoreType<F<A, B>>, Type<F<A, B>> {
    @Override
    public <Y> Type<F<A, B>> unify(Type<Y> right) throws TypeCheckException {
        if (!(right instanceof FunctionType<?, ?> funType)) {
            throw new TypeCheckException(this, right);
        }
        return new FunctionType<>(domain.unify(funType.domain), range.unify(funType.range));
    }

    @Override
    public <Z> Signature<V<Z, F<A, B>>> pointFree(VarType<Z> argument) {
        return new Signature.Function<>(domain.pointFree(argument), range.pointFree(argument));
    }

    @Override
    public String toString() {
        return "{" + domain + " → " + range + "}";
    }
}

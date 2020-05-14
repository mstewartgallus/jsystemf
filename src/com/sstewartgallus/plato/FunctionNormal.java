package com.sstewartgallus.plato;

import com.sstewartgallus.ir.Signature;

public record FunctionNormal<A, B>(Type<A>domain, Type<B>range) implements NormalType<F<A, B>>, CoreType<F<A, B>> {
    @Override
    public <Y> Type<F<A, B>> unify(Type<Y> right) throws TypeCheckException {
        if (!(right instanceof FunctionNormal<?, ?> funType)) {
            throw new TypeCheckException(this, right);
        }
        return new FunctionNormal<>(domain.unify(funType.domain), range.unify(funType.range));
    }

    @Override
    public <Z> Type<F<A, B>> substitute(Id<Z> v, Type<Z> replacement) {
        return new FunctionNormal<>(domain.substitute(v, replacement), range.substitute(v, replacement));
    }

    @Override
    public <Z> Signature<V<Z, F<A, B>>> pointFree(Id<Z> argument, IdGen vars) {
        return new Signature.Function<>(domain.pointFree(argument, vars), range.pointFree(argument, vars));
    }

    @Override
    public String toString() {
        return "{" + domain + " → " + range + "}";
    }
}

package com.sstewartgallus.plato;

import com.sstewartgallus.runtime.TypeDesc;

import java.util.Optional;

public record FunctionType<A, B>() implements CoreType<V<A, V<B, F<A, B>>>>, Type<V<A, V<B, F<A, B>>>> {
    @Override
    public Optional<TypeDesc<V<A, V<B, F<A, B>>>>> describeConstable() {
        return Optional.of(TypeDesc.ofFunction());
    }

    @Override
    public <Y> Type<V<A, V<B, F<A, B>>>> unify(Type<Y> right) throws TypeCheckException {
        if (!(right instanceof FunctionType<?, ?> funType)) {
            throw new TypeCheckException(this, right);
        }
        return this;
    }

    public Class<?> erase() {
        return Term.class;
    }
}
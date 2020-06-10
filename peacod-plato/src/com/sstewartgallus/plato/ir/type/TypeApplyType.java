package com.sstewartgallus.plato.ir.type;

import com.sstewartgallus.plato.runtime.V;

import java.util.Objects;
import java.util.Optional;

abstract class TypeApplyType<A, B> implements Type<B> {
    public final Type<A> x;
    public final Type<V<A, B>> f;

    public TypeApplyType(Type<V<A, B>> f, Type<A> x) {
        this.f = Objects.requireNonNull(f);
        this.x = Objects.requireNonNull(x);
    }

    @Override
    public final Optional<TypeDesc<B>> describeConstable() {
        var fConst = f.describeConstable();
        var xConst = x.describeConstable();
        if (fConst.isEmpty() || xConst.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(TypeDesc.ofApply(fConst.get(), xConst.get()));
    }

    @Override
    public final String toString() {
        return "(" + f + " " + x + ")";
    }
}
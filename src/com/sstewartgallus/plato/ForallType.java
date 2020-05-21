package com.sstewartgallus.plato;

import java.util.function.Function;

public record ForallType<A, B>(Function<Type<A>, Type<B>>f) implements Type<V<A, B>> {
    @Override
    public <Y> Type<V<A, B>> unify(Type<Y> right) {
        throw new UnsupportedOperationException("unimplemented");
    }
}

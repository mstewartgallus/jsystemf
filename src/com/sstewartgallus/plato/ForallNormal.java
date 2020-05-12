package com.sstewartgallus.plato;

import java.util.function.Function;

public record ForallNormal<A, B>(Function<Type<A>, Type<B>>f) implements NormalType<V<A, B>>, CoreType<V<A, B>> {
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    public String toString() {
        var depth = DEPTH.get();
        DEPTH.set(depth + 1);

        String str;
        try {
            var t = new VarType<>(new Id<A>(depth));
            str = "{forall " + t + ". " + f.apply(t) + "}";
        } finally {
            DEPTH.set(depth);
            if (depth == 0) {
                DEPTH.remove();
            }
        }
        return str;
    }

    @Override
    public <Y> Type<V<A, B>> unify(Type<Y> right) {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public <T> Type<V<A, B>> substitute(Id<T> v, Type<T> replacement) {
        throw new UnsupportedOperationException("unimplemented");
    }
}

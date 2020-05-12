package com.sstewartgallus.plato;

public record ExistentialNormal<A, B>(Type<A>x, Type<B>y) implements NormalType<E<A, B>> {
    @Override
    public String toString() {
        return "{exists " + x + ". " + y + "}";
    }

    @Override
    public <Y> Type<E<A, B>> unify(Type<Y> right) {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public <L> L visit(Visitor<L, E<A, B>> visitor) {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public <T> Type<E<A, B>> substitute(Id<T> v, Type<T> replacement) {
        throw new UnsupportedOperationException("unimplemented");
    }
}

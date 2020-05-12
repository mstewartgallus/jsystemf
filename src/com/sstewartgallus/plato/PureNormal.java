package com.sstewartgallus.plato;

// fixme... rename/retype, not clear enough this creates a new type...
public record PureNormal<A>(Class<A>clazz) implements NormalType<A> {
    @Override
    public <L> L visit(Visitor<L, A> visitor) {
        return visitor.onPureType(clazz);
    }

    public <Y> Type<A> unify(Type<Y> right) throws TypeCheckException {
        if (this != right) {
            throw new TypeCheckException(this, right);
        }
        return this;
    }

    @Override
    public <Z> Type<A> substitute(Id<Z> v, Type<Z> replacement) {
        return new PureNormal<>(clazz);
    }

    @Override
    public String toString() {
        return clazz.getCanonicalName();
    }
}

package com.sstewartgallus.plato;

import java.util.Objects;

public record ExistsValue<A, B>(Type<A>x, Term<B>y) implements ValueTerm<E<A, B>> {
    public ExistsValue {
        Objects.requireNonNull(x);
        Objects.requireNonNull(y);
    }

    @Override
    public <X> X visit(Visitor<X, E<A, B>> visitor) {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public Type<E<A, B>> type() throws TypeCheckException {
        return new Type.Exists<>(x, y.type());
    }

    @Override
    public String toString() {
        return "{exists " + x + ". " + y + "}";
    }
}

package com.sstewartgallus.plato;

import java.util.Objects;
import java.util.function.Function;

public record TypeLambdaValue<A, B>(Function<Type<A>, Term<B>>f) implements ValueTerm<V<A, B>>, CoreTerm<V<A, B>> {
    public TypeLambdaValue {
        Objects.requireNonNull(f);
    }

    @Override
    public Type<V<A, B>> type() throws TypeCheckException {
        // fixme... pass in the variable generator?
        var v = new Id<A>(0);
        var body = f.apply(new VarType<>(v)).type();
        return Type.v(x -> body.substitute(v, x));
    }

    @Override
    public String toString() {
        var dummy = new VarType<>(new Id<A>(0));
        return "{forall " + dummy + ". " + f.apply(dummy) + "}";
    }
}

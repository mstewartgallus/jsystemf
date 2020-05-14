package com.sstewartgallus.plato;

import com.sstewartgallus.ext.pretty.PrettyType;

import java.util.Objects;
import java.util.function.Function;

public record TypeLambdaValue<A, B>(Function<Type<A>, Term<B>>f) implements ValueTerm<V<A, B>>, CoreTerm<V<A, B>> {
    public TypeLambdaValue {
        Objects.requireNonNull(f);
    }

    @Override
    public Type<V<A, B>> type() {
        return Type.v(x -> f.apply(x).type());
    }

    @Override
    public String toString() {
        try (var pretty = PrettyType.<A>generate()) {
            var body = f.apply(pretty);
            return "(∀" + pretty + " → " + body + ")";
        }
    }
}

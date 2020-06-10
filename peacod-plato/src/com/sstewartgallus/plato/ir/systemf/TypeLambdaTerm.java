package com.sstewartgallus.plato.ir.systemf;

import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.V;

import java.util.Objects;
import java.util.function.Function;

public record TypeLambdaTerm<A, B>(Function<TypeDesc<A>, Term<B>>f) implements Term<V<A, B>> {
    public TypeLambdaTerm {
        Objects.requireNonNull(f);
    }

    @Override
    public TypeDesc<V<A, B>> type() {
        throw null;  // fixme...
        //    return Type.v(x -> f.apply(x).type());
    }

}

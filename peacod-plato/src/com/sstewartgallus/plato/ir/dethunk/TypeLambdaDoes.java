package com.sstewartgallus.plato.ir.dethunk;

import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.V;
import com.sstewartgallus.plato.runtime.type.Type;

public abstract class TypeLambdaDoes<A, B> implements Does<V<A, B>> {
    @Override
    public TypeDesc<V<A, B>> type() {
        return null;
    }

    public abstract Does<B> apply(Type<A> x);
}

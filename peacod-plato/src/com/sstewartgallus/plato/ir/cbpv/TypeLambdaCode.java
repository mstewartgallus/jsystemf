package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.type.Type;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.V;

public abstract class TypeLambdaCode<A, B> implements Code<V<A, B>> {

    @Override
    public TypeDesc<V<A, B>> type() {
        return null;
    }

    public abstract Code<B> apply(Type<A> x);
}

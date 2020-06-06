package com.sstewartgallus.plato.cbpv;

import com.sstewartgallus.plato.runtime.V;
import com.sstewartgallus.plato.syntax.type.Type;

public abstract class TypeLambdaCode<A, B> implements Code<V<A, B>> {

    @Override
    public Type<V<A, B>> type() {
        var self = this;
        return Type.v((Type<A> x) -> self.apply(x).type());
    }

    public abstract Code<B> apply(Type<A> x);
}

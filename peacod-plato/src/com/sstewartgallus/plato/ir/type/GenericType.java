package com.sstewartgallus.plato.ir.type;

import com.sstewartgallus.plato.runtime.V;

interface GenericType<A, B> extends Type<V<A, B>> {
    Type<B> apply(Type<A> x);
}
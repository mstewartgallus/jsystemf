package com.sstewartgallus.plato.runtime;

import com.sstewartgallus.plato.ir.type.RealType;
import com.sstewartgallus.plato.ir.type.Type;

public abstract class FnImpl<A, B> implements U<Fn<A, B>> {
    private final RealType<A> domain;

    public FnImpl(Type<A> domain) {
        this.domain = (RealType<A>) domain;
    }

    public abstract U<B> apply(A value);

}

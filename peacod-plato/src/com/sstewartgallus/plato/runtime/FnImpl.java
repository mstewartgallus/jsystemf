package com.sstewartgallus.plato.runtime;

import com.sstewartgallus.plato.runtime.type.RealType;
import com.sstewartgallus.plato.runtime.type.Stk;
import com.sstewartgallus.plato.runtime.type.Type;
import com.sstewartgallus.plato.runtime.type.U;

public abstract class FnImpl<A, B> extends U<Fn<A, B>> {
    private final RealType<A> domain;

    public FnImpl(Type<A> domain) {
        this.domain = (RealType<A>) domain;
    }

    public abstract U<B> apply(A value);

    @Override
    public final <C> void enter(Continuation<C> context, Stk<Fn<A, B>> stack) {
        var push = (PushStk<A, B>) stack;
        context.saveOrEnter(apply(push.head()), push.tail());
    }
}

package com.sstewartgallus.plato.runtime;

import com.sstewartgallus.plato.runtime.type.Stk;

public interface FreeStk<A> extends Stk<F<A>> {
    <C> void enter(Continuation<C> context, A value);
}

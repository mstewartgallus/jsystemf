package com.sstewartgallus.plato.runtime;

import com.sstewartgallus.plato.runtime.type.U;

// fixme... make abstract base class ?
public abstract class FreeImpl<A> extends U<F<A>> {
    public abstract A evaluate();


}

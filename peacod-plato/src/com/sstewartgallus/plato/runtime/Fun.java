package com.sstewartgallus.plato.runtime;

import com.sstewartgallus.plato.runtime.type.U;

public interface Fun<A, B> {
    U<B> apply(U<A> value);
}

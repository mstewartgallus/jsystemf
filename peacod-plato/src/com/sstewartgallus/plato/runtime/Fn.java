package com.sstewartgallus.plato.runtime;

import com.sstewartgallus.plato.runtime.type.U;

public interface Fn<A, B> {
    U<B> apply(A value);
}

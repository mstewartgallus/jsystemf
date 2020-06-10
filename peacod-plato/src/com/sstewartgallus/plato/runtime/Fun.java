package com.sstewartgallus.plato.runtime;

public interface Fun<A, B> {
    U<B> apply(U<A> value);
}

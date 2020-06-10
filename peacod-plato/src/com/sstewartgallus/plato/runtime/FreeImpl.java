package com.sstewartgallus.plato.runtime;

public interface FreeImpl<A> extends U<F<A>> {
    A evaluate();
}

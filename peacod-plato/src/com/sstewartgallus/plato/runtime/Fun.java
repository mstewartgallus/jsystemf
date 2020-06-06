package com.sstewartgallus.plato.runtime;

public interface Fun<A, B> extends U<Fun<A, B>> {
    U<B> apply(A value);

    default Fun<A, B> action() {
        return this;
    }
}

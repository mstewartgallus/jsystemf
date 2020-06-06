package com.sstewartgallus.plato.runtime;

public interface F<A> extends U<F<A>> {
    A evaluate();

    default F<A> action() {
        return this;
    }
}

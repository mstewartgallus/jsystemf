package com.sstewartgallus.runtime;

@FunctionalInterface
interface Stack<A, B> {
    Frame<B> step(A result);
}

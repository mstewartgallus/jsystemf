package com.sstewartgallus.interpreter;

@FunctionalInterface
interface Stack<A, B> {
    Frame<B> step(A result);
}

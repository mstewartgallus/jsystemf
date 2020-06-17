package com.sstewartgallus.plato.runtime;

public record FnStack<C, A, B>(A value, Stack<C, B>next) implements Stack<C, Fn<A, B>> {
}

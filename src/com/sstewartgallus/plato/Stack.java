package com.sstewartgallus.plato;

@FunctionalInterface
interface Stack<A, B> {
    State<B> step(Interpreter<?, B> interp, Term<A> term);
}

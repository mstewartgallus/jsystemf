package com.sstewartgallus.plato;

@FunctionalInterface
interface Stack<A, B> {
    Interpreter<?, B> step(Interpreter<?, B> interp, Term<A> term);
}

package com.sstewartgallus.interpreter;

@FunctionalInterface
interface Stack<A, B> {
    Frame<B> step(A result);

    default Interpreter<?, B> returnWith(ReferenceInterpreter<A, B> interpreter, A value) {
        return step(value).returnTo(interpreter);
    }
}

package com.sstewartgallus.interpreter;

interface Frame<A> {
    <X> Interpreter<?, A> returnTo(Interpreter<X, A> interpreter);
}

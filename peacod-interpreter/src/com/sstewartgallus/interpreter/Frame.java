package com.sstewartgallus.interpreter;

interface Frame<A> {
    <X> Interpreter<?, A> returnTo(ReferenceInterpreter<X, A> interpreter);
}

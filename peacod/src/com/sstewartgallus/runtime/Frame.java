package com.sstewartgallus.runtime;

import com.sstewartgallus.interpreter.Interpreter;

interface Frame<A> {
    <X> Interpreter<?, A> returnTo(Interpreter<X, A> interpreter);
}

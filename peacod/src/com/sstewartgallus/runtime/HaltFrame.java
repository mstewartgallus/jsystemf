package com.sstewartgallus.runtime;

import com.sstewartgallus.interpreter.Interpreter;

record HaltFrame<A>(A value) implements Frame<A> {
    @Override
    public <X> Interpreter<?, A> returnTo(Interpreter<X, A> interpreter) {
        return new JitInterpreter<>(null, null, value);
    }
}

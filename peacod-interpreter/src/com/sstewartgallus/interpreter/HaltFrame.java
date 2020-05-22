package com.sstewartgallus.interpreter;

record HaltFrame<A>(A value) implements Frame<A> {
    @Override
    public <X> Interpreter<?, A> returnTo(ReferenceInterpreter<X, A> interpreter) {
        return new ReferenceInterpreter<>(null, interpreter.env, null, value);
    }
}

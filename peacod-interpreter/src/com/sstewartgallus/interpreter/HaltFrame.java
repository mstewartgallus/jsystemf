package com.sstewartgallus.interpreter;

record HaltFrame<A>(A value) implements Frame<A> {
    @Override
    public <X> Interpreter<?, A> returnTo(Interpreter<X, A> interpreter) {
        return new ReferenceInterpreter<>(null, null, value);
    }
}

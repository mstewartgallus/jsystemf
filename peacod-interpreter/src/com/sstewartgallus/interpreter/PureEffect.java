package com.sstewartgallus.interpreter;

record PureEffect<A>(A value) implements Effect<A> {
    @Override
    public <B> Interpreter<?, B> execute(Interpreter<A, B> interpreter) {
        return interpreter.pure(value);
    }
}

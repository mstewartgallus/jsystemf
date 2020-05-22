package com.sstewartgallus.interpreter;

record ContinueFrame<A, B>(Effect<A>ip, Stack<A, B>prev) implements Frame<B> {
    public <X> Interpreter<?, B> returnTo(ReferenceInterpreter<X, B> interpreter) {
        return new ReferenceInterpreter<>(ip, interpreter.env, prev, null);
    }
}

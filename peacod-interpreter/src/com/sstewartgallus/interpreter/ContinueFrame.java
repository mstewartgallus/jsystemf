package com.sstewartgallus.interpreter;

record ContinueFrame<A, B>(Effect<A>ip, Stack<A, B>prev) implements Frame<B> {
    public <X> Interpreter<?, B> returnTo(Interpreter<X, B> interpreter) {
        return new ReferenceInterpreter<>(ip, prev, null);
    }
}

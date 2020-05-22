package com.sstewartgallus.runtime;

import com.sstewartgallus.interpreter.Effect;
import com.sstewartgallus.interpreter.Interpreter;

record ContinueFrame<A, B>(Effect<A>ip, Stack<A, B>prev) implements Frame<B> {
    public <X> Interpreter<?, B> returnTo(Interpreter<X, B> interpreter) {
        return new JitInterpreter<>(ip, prev, null);
    }
}

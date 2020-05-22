package com.sstewartgallus.interpreter;

import java.util.function.Function;

record BindEffect<A, B>(Effect<A>x, Function<A, Effect<B>>f) implements Effect<B> {
    @Override
    public <C> Interpreter<?, C> execute(Interpreter<B, C> interpreter) {
        return interpreter.bind(x, f);
    }
}

package com.sstewartgallus.interpreter;

public record LoadEffect<X, C>(Interpreter.Equal<X, Effect<C>>witness, Id<X>effectId) implements Effect<C> {
    @Override
    public <B> Interpreter<?, B> execute(Interpreter<C, B> interpreter) {
        return interpreter.load(witness, effectId);
    }
}

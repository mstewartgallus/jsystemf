package com.sstewartgallus.interpreter;

public record ThunkEffect<A>(Effect<A>effect) implements Effect<Effect<A>> {
    @Override
    public <B> Interpreter<?, B> execute(Interpreter<Effect<A>, B> interpreter) {
        return interpreter.thunk(new Interpreter.Equal<>(new Interpreter.Subclasses<>(), new Interpreter.Subclasses<>()),
                effect);
    }
}


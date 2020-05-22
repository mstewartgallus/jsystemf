package com.sstewartgallus.interpreter;

public record ThunkEffect<A>(Effect<A>effect) implements Effect<Effect<A>> {
    @Override
    public <B> Interpreter<?, B> execute(Interpreter<Effect<A>, B> interpreter) {
        return null;
    }
}


package com.sstewartgallus.interpreter;

public record ResetFrame<X, A>(Environment env, Frame<A>step) implements Frame<A> {
    @Override
    public <X> Interpreter<?, A> returnTo(ReferenceInterpreter<X, A> interpreter) {
        return step.returnTo(new ReferenceInterpreter<>(interpreter.ip, env, interpreter.stack, null));
    }
}

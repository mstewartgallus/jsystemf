package com.sstewartgallus.runtime;

import com.sstewartgallus.interpreter.Effect;
import com.sstewartgallus.interpreter.Id;
import com.sstewartgallus.interpreter.Interpreter;

import java.util.function.Function;

/**
 * This will be a jit interpreter based sort of around partial evaluation.
 */
public final class JitInterpreter<X, A> extends Interpreter<X, A> {
    private final Stack<X, A> stack;
    private final Effect<X> ip;
    private final A halted;

    JitInterpreter(Effect<X> ip, Stack<X, A> stack, A halted) {
        this.ip = ip;
        this.stack = stack;
        this.halted = halted;
    }

    @Override
    protected Interpreter<?, A> step() {
        return ip.execute(this);
    }

    @Override
    protected boolean halted() {
        return halted != null;
    }

    @Override
    protected A result() {
        return halted;
    }

    @Override
    public Interpreter<?, A> pure(X value) {
        return stack.step(value).returnTo(this);
    }

    @Override
    public <C> Interpreter<?, A> thunk(Equal<X, Effect<C>> witness, X effect) {
        Effect<C> reallyEffect = witness.left().to(effect);
        return new JitInterpreter<>(reallyEffect, (C evaluated) -> {
            var from = witness.right().to(Effect.pure(evaluated));
            return stack.step(from);
        }, null);
    }

    @Override
    public <C> Interpreter<?, A> load(Equal<C, Effect<X>> witness, Id<C> effectId) {
        throw null;
    }

    @Override
    public <Z> Interpreter<?, A> bind(Effect<Z> x, Function<Z, Effect<X>> f) {
        return new JitInterpreter<>(x, (evaluated) -> {
            var y = f.apply(evaluated);
            return new ContinueFrame<>(y, stack);
        }, null);
    }

}

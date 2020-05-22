package com.sstewartgallus.runtime;

import com.sstewartgallus.interpreter.Effect;
import com.sstewartgallus.interpreter.Interpreter;

import java.util.function.Function;

/**
 * This will be a simple obviously correct interpreter.
 * <p>
 * This shall form the bootstrap to a metacircular approach where we interpret the JIT and then JIT the interpreter and
 * the JIT.
 * <p>
 * Loosely based around CESK
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
    public <Z> Interpreter<?, A> bind(Effect<Z> x, Function<Z, Effect<X>> f) {
        return new JitInterpreter<>(x, (evaluated) -> {
            var y = f.apply(evaluated);
            return new ContinueFrame<>(y, stack);
        }, null);
    }

}

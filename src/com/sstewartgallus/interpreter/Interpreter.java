package com.sstewartgallus.interpreter;

import java.util.function.Function;

/**
 * This will be a simple obviously correct interpreter.
 * <p>
 * This shall form the bootstrap to a metacircular approach where we interpret the JIT and then JIT the interpreter and
 * the JIT.
 * <p>
 * Loosely based around CESK
 */
public final class Interpreter<X, A> {
    private final Stack<X, A> stack;
    private final Effect<X> ip;
    private final A halted;

    private Interpreter(Effect<X> ip, Stack<X, A> stack, A halted) {
        this.ip = ip;
        this.stack = stack;
        this.halted = halted;
    }

    public static <A> A interpret(Effect<A> init) {
        Interpreter<?, A> interp = new Interpreter<>(init, Halt::new, null);
        do {
            interp = interp.step();
        } while (interp.halted == null);
        return interp.halted;
    }

    private Interpreter<?, A> step() {
        return ip.step(this);
    }

    public Interpreter<?, A> returnWith(X value) {
        return stack.step(value).returnTo(this);
    }

    public <Z> Interpreter<?, A> bind(Effect<Z> x, Function<Z, Effect<X>> f) {
        return new Interpreter<>(x, (evaluated) -> {
            var y = f.apply(evaluated);
            return new ContinueFrame<>(y, stack);
        }, null);
    }

    @FunctionalInterface
    interface Stack<A, B> {
        Frame<B> step(A result);
    }

    interface Frame<A> {
        <X> Interpreter<?, A> returnTo(Interpreter<X, A> interpreter);
    }

    record ContinueFrame<A, B>(Effect<A>ip, Stack<A, B>prev) implements Frame<B> {
        public <X> Interpreter<?, B> returnTo(Interpreter<X, B> interpreter) {
            return new Interpreter<>(ip, prev, null);
        }
    }

    record Halt<A>(A value) implements Frame<A> {
        @Override
        public <X> Interpreter<?, A> returnTo(Interpreter<X, A> interpreter) {
            return new Interpreter<>(null, null, value);
        }
    }
}

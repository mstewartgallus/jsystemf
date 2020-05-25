package com.sstewartgallus.interpreter;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * This will be a simple obviously correct interpreter.
 * <p>
 * This shall form the bootstrap to a metacircular approach where we interpret the JIT and then JIT the interpreter and
 * the JIT.
 * <p>
 * Loosely based around CESK
 */
public final class ReferenceInterpreter<X, A> extends Interpreter<X, A> {
    final Environment env;
    final Stack<X, A> stack;
    final Code<X> ip;
    private final A halted;

    ReferenceInterpreter(Code<X> ip, Environment environment, Stack<X, A> stack, A halted) {
        this.ip = ip;
        this.env = environment;
        this.stack = stack;
        this.halted = halted;
    }

    @Override
    public Interpreter<?, A> pure(X value) {
        return stack.returnWith(this, value);
    }

    @Override
    public Interpreter<?, A> loop(Code<X> init, Predicate<X> pred, Function<X, X> body) {
        return new ReferenceInterpreter<>(init, env, createLoop(pred, body), null);
    }

    // fixme... should be possible to use same bits every part of a loop..
    private Stack<X, A> createLoop(Predicate<X> pred, Function<X, X> body) {
        return initVal -> {
            if (pred.test(initVal)) {
                var nextVal = body.apply(initVal);
                return new ContinueFrame<>(new PureCode<>(nextVal), createLoop(pred, body));
            }
            return stack.step(initVal);
        };
    }

    @Override
    public <C> Interpreter<?, A> apply(Code<Function<C, X>> f, Code<C> x) {
        return new ReferenceInterpreter<>(f, env, fEval ->
                new ContinueFrame<>(x, xEval -> stack.step(fEval.apply(xEval))), null);
    }

    @Override
    protected Interpreter<?, A> step() {
        System.err.println("step " + ip);
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

    public String toString() {
        return env + " " + stack + " " + ip + " " + halted;
    }
}

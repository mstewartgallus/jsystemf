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
public final class ReferenceInterpreter<X, A> extends Interpreter<X, A> {
    final Environment env;
    private final Stack<X, A> stack;
    private final Effect<X> ip;
    private final A halted;

    ReferenceInterpreter(Effect<X> ip, Environment environment, Stack<X, A> stack, A halted) {
        this.ip = ip;
        this.env = environment;
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
        var effectId = new Id<X>();
        effectId.value = witness.right().to(witness.left().to(effect).bind((C value) -> {
            effectId.value = witness.right().to(Effect.pure(value));
            return Effect.pure(value);
        }));
        return stack.step(Effect.load(witness, effectId)).returnTo(this);
    }

    @Override
    public <C> Interpreter<?, A> load(Equal<C, Effect<X>> witness, Id<C> effectId) {
        var result = effectId.value;
        var effect = witness.left().to(result);
        return effect.execute(this);
    }

    @Override
    public <Z> Interpreter<?, A> bind(Effect<Z> x, Function<Z, Effect<X>> f) {
        return new ReferenceInterpreter<>(x, env, (evaluated) -> {
            var y = f.apply(evaluated);
            return new ContinueFrame<>(y, stack);
        }, null);
    }

}

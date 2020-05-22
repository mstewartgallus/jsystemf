package com.sstewartgallus.plato;

import java.util.Map;
import java.util.WeakHashMap;
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
    private final Term<X> ip;
    private final Term<A> halted;
    private final Map<VarTerm<?>, Term<?>> environment;

    private Interpreter(Term<X> ip, Stack<X, A> stack, Term<A> halted, Map<VarTerm<?>, Term<?>> environment) {
        this.ip = ip;
        this.stack = stack;
        this.halted = halted;
        this.environment = environment;
    }

    public static <A> ValueTerm<A> normalize(Term<A> term) {
        Interpreter<?, A> interp = new Interpreter<>(term, Interpreter::halt, null, Map.of());
        do {
            interp = interp.step();
        } while (interp.halted == null);
        return (ValueTerm<A>) interp.halted;
    }

    private static <A> Interpreter<A, A> halt(Interpreter<?, A> interpreter, Term<A> value) {
        return new Interpreter<>(null, null, value, interpreter.environment);
    }

    private Interpreter<?, A> step() {
        return ip.step(this);
    }

    public <Z> Interpreter<X, A> put(VarTerm<Z> value, Term<Z> term) {
        var map = new WeakHashMap<>(environment);
        map.put(value, term);
        return new Interpreter<>(ip, stack, null, map);
    }

    public <Z> Term<Z> lookup(VarTerm<Z> value) {
        return (Term<Z>) environment.get(value);
    }

    public Interpreter<?, A> returnWith(Term<X> value) {
        return stack.step(this, value);
    }

    public <Z> Interpreter<?, A> evaluate(Term<Z> value, Function<Term<Z>, Term<X>> k) {
        return new Interpreter<>(value, (iterp, evaluated) -> {
            var y = k.apply(evaluated);
            return stack.step(iterp, y);
        }, null, environment);
    }

    @FunctionalInterface
    interface Stack<A, B> {
        Interpreter<?, B> step(Interpreter<?, B> interp, Term<A> term);
    }
}

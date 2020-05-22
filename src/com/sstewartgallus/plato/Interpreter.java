package com.sstewartgallus.plato;

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

    private Interpreter(Term<X> ip, Stack<X, A> stack, Term<A> halted) {
        this.ip = ip;
        this.stack = stack;
        this.halted = halted;
    }

    public static <A> ValueTerm<A> normalize(Term<A> term) {
        Interpreter<?, A> interp = new Interpreter<>(term, (i, value) -> new Interpreter<>(value, null, value), null);
        do {
            interp = interp.step();
        } while (interp.halted == null);
        return (ValueTerm<A>) interp.halted;
    }

    private Interpreter<?, A> step() {
        return ip.step(this);
    }

    public Interpreter<?, A> returnWith(Term<X> value) {
        return stack.step(this, value);
    }

    public <Z> Interpreter<?, A> eval(Term<Z> value, Function<Term<Z>, Term<X>> k) {
        return new Interpreter<>(value, (iterp, evaluated) -> {
            var y = k.apply(evaluated);
            return stack.step(iterp, y);
        }, null);
    }
}

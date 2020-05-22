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
public record Interpreter<X, A>(Term<X>ip, Stack<X, A>stack) implements State<A> {
    public State<A> returnWith(Term<X> value) {
        return stack.step(this, value);
    }

    public <Z> State<A> eval(Term<Z> value, Function<Term<Z>, Term<X>> k) {
        return new Interpreter<>(value, (iterp, evaluated) -> {
            var y = k.apply(evaluated);
            return stack.step(iterp, y);
        });
    }
}

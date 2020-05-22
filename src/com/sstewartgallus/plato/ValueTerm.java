package com.sstewartgallus.plato;

/**
 * This is a convenience interface for terms that are always considered normalized
 */
public interface ValueTerm<A> extends Term<A> {
    @Override
    default <X> State<X> step(Interpreter<A, X> interpreter) {
        return interpreter.returnWith(this);
    }
}

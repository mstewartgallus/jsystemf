package com.sstewartgallus.plato;

/**
 * This will be a simple obviously correct interpreter.
 * <p>
 * This shall form the bootstrap to a metacircular approach where we interpret the JIT and then JIT the interpreter and
 * the JIT.
 */
public final class Interpreter {
    private Interpreter() {
    }

    // fixme... pass in a context?
    // fixme... just make part of the data type?
    // fixme... move else where?
    public static <A> ValueTerm<A> normalize(Term<A> term) {
        while (term instanceof ThunkTerm<A> thunk) {
            term = thunk.stepThunk();
        }
        return (ValueTerm<A>) term;
    }
}

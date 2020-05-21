package com.sstewartgallus.plato;

import com.sstewartgallus.ext.java.IntValue;

interface State<A> {
}

@FunctionalInterface
interface Stack<A, B> {
    State<B> step(Interpreter<?, B> interp, Term<A> term);
}

/**
 * This will be a simple obviously correct interpreter.
 * <p>
 * This shall form the bootstrap to a metacircular approach where we interpret the JIT and then JIT the interpreter and
 * the JIT.
 */

public record Interpreter<X, A>(Term<X>ip, Stack<X, A>stack) implements State<A> {

    // fixme... pass in a context?
    // fixme... just make part of the data type?
    // fixme... move else where?
    // fixme... get away from value term...
    public static <A> ValueTerm<A> normalize(Term<A> term) {
        Interpreter<?, A> interp = new Interpreter<>(term, (iterp, value) -> new Halt<>(value));
        for (; ; ) {
            var next = interp.step();
            if (next instanceof Interpreter<?, A> nextK) {
                interp = nextK;
                continue;
            }
            if (next instanceof Halt<A> halt) {
                return (ValueTerm<A>) halt.value();
            }
            throw new IllegalStateException(next.toString());
        }
    }

    public State<A> step() {
        var term = ip;
        if (term instanceof IntValue || term instanceof LambdaTerm) {
            return stack.step(this, term);
        }
        if (term instanceof ApplyTerm<?, X> apply) {
            return stepApply(apply);
        }
        throw new UnsupportedOperationException(term.toString());
    }

    private <C> State<A> stepApply(ApplyTerm<C, X> apply) {
        var f = apply.f();
        var x = apply.x();

        return new Interpreter<>(f, (iterp, fValue) -> {
            var fLambda = ((LambdaTerm<C, X>) fValue);
            return stack.step(iterp, fLambda.apply(x));
        });
    }
}

record Halt<A>(Term<A>value) implements State<A> {
}
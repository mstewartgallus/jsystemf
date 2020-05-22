package com.sstewartgallus.plato;

public interface State<A> {
    static <A> ValueTerm<A> normalize(Term<A> term) {
        Interpreter<?, A> interp = new Interpreter<>(term, (i, value) -> new Halt<>(value));
        for (; ; ) {
            var next = step(interp);
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

    private static <A, B> State<B> step(Interpreter<A, B> interp) {
        return interp.ip().step(interp);
    }
}

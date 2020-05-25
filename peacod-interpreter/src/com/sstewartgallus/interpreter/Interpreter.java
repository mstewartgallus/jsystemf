package com.sstewartgallus.interpreter;

import java.util.function.Function;
import java.util.function.Predicate;

public abstract class Interpreter<X, A> {
    public static <A> A execute(Code<A> init) {
        var interp = new ReferenceInterpreter<>(init, new Environment(), HaltFrame::new, null);
        return interp.execute();
    }

    public A execute() {
        Interpreter<?, A> current = this;
        do {
            current = current.step();
        } while (!current.halted());
        return current.result();
    }

    protected abstract Interpreter<?, A> step();

    protected abstract boolean halted();

    protected abstract A result();

    public abstract Interpreter<?, A> pure(X value);
    public abstract Interpreter<?, A> loop(Code<X> init, Predicate<X> pred, Function<X, X> body);
    public abstract <C> Interpreter<?,A> apply(Code<Function<C, X>> f, Code<C> x);
}

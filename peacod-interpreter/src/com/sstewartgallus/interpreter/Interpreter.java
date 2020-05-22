package com.sstewartgallus.interpreter;

import java.util.function.Function;

public abstract class Interpreter<X, A> {
    public static <A> A execute(Effect<A> init) {
        var interp = new ReferenceInterpreter<>(init, HaltFrame::new, null);
        return interp.execute();
    }

    public A execute() {
        Interpreter<?, A> current = this;
        do {
            current = current.step();
        } while (!current.halted());
        return current.result();
    }

    public abstract Interpreter<?, A> pure(X value);

    public abstract <Z> Interpreter<?, A> bind(Effect<Z> x, Function<Z, Effect<X>> f);

    protected abstract Interpreter<?, A> step();

    protected abstract boolean halted();

    protected abstract A result();}

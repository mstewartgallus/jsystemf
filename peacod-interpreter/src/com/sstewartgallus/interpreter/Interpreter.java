package com.sstewartgallus.interpreter;

import java.util.function.Function;

public abstract class Interpreter<X, A> {
    public static <A> A execute(Effect<A> init) {
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

    public abstract Interpreter<?, A> pure(X value);

    public abstract <C> Interpreter<?, A> thunk(Equal<X, Effect<C>> x, X effect);

    public abstract <C> Interpreter<?, A> load(Equal<C, Effect<X>> witness, Id<C> effectId);

    public abstract <Z> Interpreter<?, A> bind(Effect<Z> x, Function<Z, Effect<X>> f);

    protected abstract Interpreter<?, A> step();

    protected abstract boolean halted();

    protected abstract A result();


    public record Equal<A, B>(Subclasses<? super A, B>left, Subclasses<? super B, A>right) {
    }

    public record Subclasses<A extends B, B>() {
        public B to(A x) {
            return x;
        }
    }
}

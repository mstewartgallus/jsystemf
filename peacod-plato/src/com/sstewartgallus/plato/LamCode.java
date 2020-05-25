package com.sstewartgallus.plato;

import com.sstewartgallus.interpreter.*;

import java.util.function.Function;

public record LamCode<A, B>(Type<A> domain, Type<B> range) implements Code<Function<Function<Term<A>, Term<B>>, Term<F<A, B>>>> {
    @Override
    public String toString() {
        return "lam";
    }

    @Override
    public <X> Interpreter<?, X> execute(Interpreter<Function<Function<Term<A>, Term<B>>, Term<F<A, B>>>, X> interpreter) {
        return interpreter.pure(this::lam);
    }

    private Term<F<A, B>> lam(Function<Term<A>, Term<B>> f) {
        return new SimpleLambdaTerm<>(domain, range, f);
    }

    @Override
    public <X> Code<Function<X, Function<Function<Term<A>, Term<B>>, Term<F<A, B>>>>> pointFree(Id<X> v) {
        return new ApplyCode<>(new ConstantCode<>(), this);
    }
}

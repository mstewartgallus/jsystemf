package com.sstewartgallus.plato;

import com.sstewartgallus.interpreter.*;

import java.util.function.Function;

public record EvalCode<A, B>() implements Code<Function<Term<F<A, B>>, Function<Term<A>, Term<B>>>> {
    @Override
    public String toString() {
        return "apply";
    }

    @Override
    public <X> Interpreter<?, X> execute(Interpreter<Function<Term<F<A, B>>, Function<Term<A>, Term<B>>>, X> interpreter) {
        return interpreter.pure(EvalCode::eval);
    }

    private static <A, B> Function<Term<A>, Term<B>> eval(Term<F<A, B>> f) {
        return x -> Term.apply(f, x);
    }

    @Override
    public <X> Code<Function<X, Function<Term<F<A, B>>, Function<Term<A>, Term<B>>>>> pointFree(Id<X> v) {
        return new ApplyCode<>(new ConstantCode<>(), this);
    }
}

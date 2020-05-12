package com.sstewartgallus.pass1;

import com.sstewartgallus.plato.*;

import java.util.Objects;

public record ClosureValue<Env extends HList<Env>, A, B>(Term<F<HList.Cons<A, Env>, B>>f,
                                                         Term<Env>environment) implements LambdaValue<A, B> {
    public ClosureValue {
        Objects.requireNonNull(f);
        Objects.requireNonNull(environment);
    }

    @Override
    public Type<F<A, B>> type() throws TypeCheckException {
        var fType = f.type();

        var funType = (FunctionNormal<HList.Cons<A, Env>, B>) fType;
        var domain = ((ConsNormal<A, Env>)funType.domain()).head();

        return domain.to(funType.range());
    }

    @Override
    public Term<B> apply(Term<A> x) {
        return new TupleApplyThunk<>(f, new ConsValue<>(x, environment));
    }
}

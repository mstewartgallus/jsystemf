package com.sstewartgallus.pass1;

import com.sstewartgallus.plato.*;

import java.util.Objects;

public record ClosureValue<A extends HList<A>, X, B>(Term<F<HList.Cons<X, A>, B>>f,
                                                     Term<X>env) implements FunctionValue<A, B> {
    public ClosureValue {
        Objects.requireNonNull(f);
        Objects.requireNonNull(env);
    }

    @Override
    public Type<F<A, B>> type() throws TypeCheckException {
        var fType = f.type();

        var funType = (FunctionNormal<HList.Cons<X, A>, B>) fType;
        var domain = ((ConsNormal<X, A>) funType.domain()).tail();

        return domain.to(funType.range());
    }

    @Override
    public Term<B> apply(Term<A> x) {
        return new TupleApplyThunk<>(f, new ConsValue<>(env, x));
    }
}

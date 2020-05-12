package com.sstewartgallus.pass1;

import com.sstewartgallus.plato.*;

import java.util.Objects;
import java.util.function.Function;

public record TupleLambdaValue<A extends HList<A>, B>(Type<A>domain,
                                                      Function<Term<A>, Term<B>>f) implements FunctionValue<A, B> {
    public TupleLambdaValue {
        Objects.requireNonNull(domain);
        Objects.requireNonNull(f);
    }

    @Override
    public Type<F<A, B>> type() throws TypeCheckException {
        return domain.to(f.apply(new VarThunk<>(domain, new Id<>(0))).type());
    }

    @Override
    public Term<B> apply(Term<A> x) {
        return f.apply(x);
    }
}

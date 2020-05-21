package com.sstewartgallus.plato;

import java.util.Objects;
import java.util.function.Function;

public record TypeApplyThunk<A, B>(Term<V<A, B>>f, Type<A>x) implements ThunkTerm<B>, LambdaTerm<B> {
    public TypeApplyThunk {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    @Override
    public Term<B> visitChildren(Visitor visitor) {
        return new TypeApplyThunk<>(visitor.term(f), visitor.type(x));
    }

    @Override
    public Type<B> type() throws TypeCheckException {
        return ((ForallType<A, B>) f.type()).f().apply(x);
    }

    @Override
    public <C> Term<C> stepThunk(Function<ValueTerm<B>, Term<C>> k) {
        return f.stepThunk(fNorm -> {
            var fLambda = (TypeLambdaValue<A, B>) fNorm;
            return fLambda.apply(x).stepThunk(k);
        });
    }
}

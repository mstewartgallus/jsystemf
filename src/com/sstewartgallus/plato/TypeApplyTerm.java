package com.sstewartgallus.plato;

import java.util.Objects;

public record TypeApplyTerm<A, B>(Term<V<A, B>>f, Type<A>x) implements ThunkTerm<B> {
    public TypeApplyTerm {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    @Override
    public Term<B> visitChildren(Visitor visitor) {
        return new TypeApplyTerm<>(visitor.term(f), visitor.type(x));
    }

    @Override
    public Type<B> type() throws TypeCheckException {
        return ((ForallType<A, B>) f.type()).f().apply(x);
    }

    @Override
    public boolean reducible() {
        return true;
    }

    @Override
    public <C> Term<C> step(TermCont<B, C> k) {
        return f.step(fNorm -> {
            TypeLambdaTerm<A, B> fLambda = (TypeLambdaTerm<A, B>) fNorm;
            return fLambda.apply(x).step(k);
        });
    }
}

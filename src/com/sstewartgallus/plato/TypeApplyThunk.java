package com.sstewartgallus.plato;

import java.util.Objects;

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
    public String toString() {
        return "(" + noBrackets() + ")";
    }

    private String noBrackets() {
        if (f instanceof TypeApplyThunk<?, V<A, B>> fApply) {
            return fApply.noBrackets() + " " + x;
        }
        return f + " " + x;
    }

    @Override
    public Term<B> stepThunk() {
        // fixme... do types need to be normalized as well?
        // fixme... type check?
        var fNorm = (TypeLambdaValue<A, B>) Interpreter.normalize(f);
        // fixme... should I normalize the argument?
        return fNorm.f().apply(x);
    }
}

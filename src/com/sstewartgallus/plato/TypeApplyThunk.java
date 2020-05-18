package com.sstewartgallus.plato;

import com.sstewartgallus.runtime.TermInvoker;

import java.util.Objects;

import static java.lang.invoke.MethodHandles.lookup;

public record TypeApplyThunk<A, B>(Term<V<A, B>>f, Type<A>x) implements ThunkTerm<B>, LambdaTerm<B> {
    private static final ApplyType INVOKE_TERM = TermInvoker.newInstance(lookup(), ApplyType.class);

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
        var fNorm = Interpreter.normalize(f);
        if (fNorm instanceof TypeLambdaValue<A, B> lambda) {
            return lambda.apply(x);
        }
        return INVOKE_TERM.apply(fNorm, x);
    }

    @FunctionalInterface
    public interface ApplyType {
        <A, B> Term<B> apply(Term<V<A, B>> f, Type<A> x);
    }
}

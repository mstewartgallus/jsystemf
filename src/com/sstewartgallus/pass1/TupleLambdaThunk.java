package com.sstewartgallus.pass1;

import com.sstewartgallus.plato.*;

import java.util.Objects;
import java.util.function.Function;

// fixme... probably best to make all FunctionValue just thunks to lambdas!
public record TupleLambdaThunk<L extends HList<L>, C, D>(Sig<L, C, D>sig,
                                                         Function<L, Term<C>>f) implements ThunkTerm<D> {
    public TupleLambdaThunk {
        Objects.requireNonNull(sig);
        Objects.requireNonNull(f);
    }

    @Override
    public Type<D> type() throws TypeCheckException {
        return sig.type();
    }

    @Override
    public Term<D> stepThunk() {
        return sig.stepThunk(f);
    }

    @Override
    public <X> Term<D> substitute(Id<X> variable, Term<X> replacement) {
        return new TupleLambdaThunk<>(sig, x -> f.apply(x).substitute(variable, replacement));
    }

    @Override
    public String toString() {
        return "(" + sig.stringify(f, new IdGen()) + ")";
    }
}

package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.ext.variables.Id;
import com.sstewartgallus.ext.variables.VarValue;
import com.sstewartgallus.plato.*;

import java.util.Objects;
import java.util.function.Function;

// fixme... probably best to make all FunctionValue just thunks to lambdas!
public record UncurryLambdaThunk<L extends HList<L>, C, D>(Sig<L, C, D>sig,
                                                           Function<Term<L>, Term<C>>f) implements ThunkTerm<D> {
    public UncurryLambdaThunk {
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
        return new UncurryLambdaThunk<>(sig, x -> f.apply(x).substitute(variable, replacement));
    }

    @Override
    public String toString() {
        var t = sig.argType();
        // fixme...
        var v = new VarValue<>(t, new Id<>(0));
        return "({" + v + ": " + t + "} → " + f.apply(v) + ")";
    }

    public interface Sig<T extends HList<T>, C, D> {

        Term<D> stepThunk(Function<Term<T>, Term<C>> f);

        Type<T> argType();

        Type<D> type();

        record Zero<A>(Type<A>type) implements Sig<HList.Nil, A, A> {
            @Override
            public Term<A> stepThunk(Function<Term<HList.Nil>, Term<A>> f) {
                return f.apply(NilValue.NIL);
            }

            @Override
            public Type<HList.Nil> argType() {
                return NilNormal.NIL;
            }

            public String toString() {
                return ".";
            }
        }

        record Cons<H, T extends HList<T>, C, D>(Type<H>head,
                                                 Sig<T, C, D>tail) implements Sig<HList.Cons<H, T>, C, F<H, D>> {
            @Override
            public Term<F<H, D>> stepThunk(Function<Term<HList.Cons<H, T>>, Term<C>> f) {
                return head.l(h -> tail.stepThunk(t -> f.apply(new ConsValue<>(h, t))));
            }

            @Override
            public Type<HList.Cons<H, T>> argType() {
                return new ConsNormal<>(head, tail.argType());
            }

            @Override
            public Type<F<H, D>> type() {
                return head.to(tail.type());
            }


            public String toString() {
                return "(" + head + " Δ " + tail + ")";
            }
        }
    }
}

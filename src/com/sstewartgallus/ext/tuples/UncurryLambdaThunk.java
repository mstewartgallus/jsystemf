package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.ext.pretty.PrettyValue;
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
    public Term<D> visitChildren(Visitor visitor) {
        throw new UnsupportedOperationException("Unimplemented");
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
    public String toString() {
        var domain = sig.argType();
        try (var pretty = PrettyValue.generate(domain)) {
            var body = f.apply(pretty);
            return "({" + pretty + ": " + domain + "} → " + body + ")";
        }
    }

    public interface Sig<T extends HList<T>, C, D> {

        Term<D> stepThunk(Function<Term<T>, Term<C>> f);

        Type<T> argType();

        Type<D> type();

        record Zero<A>(Type<A>type) implements Sig<Nil, A, A> {
            @Override
            public Term<A> stepThunk(Function<Term<Nil>, Term<A>> f) {
                return f.apply(NilValue.NIL);
            }

            @Override
            public Type<Nil> argType() {
                return NilType.NIL;
            }

            public String toString() {
                return ".";
            }
        }

        record Cons<H, T extends HList<T>, C, D>(Type<H>head,
                                                 Sig<T, C, D>tail) implements Sig<com.sstewartgallus.ext.tuples.Cons<H, T>, C, F<H, D>> {
            @Override
            public Term<F<H, D>> stepThunk(Function<Term<com.sstewartgallus.ext.tuples.Cons<H, T>>, Term<C>> f) {
                return head.l(h -> tail.stepThunk(t -> f.apply(new ConsValue<>(h, t))));
            }

            @Override
            public Type<com.sstewartgallus.ext.tuples.Cons<H, T>> argType() {
                return new ConsType<>(head, tail.argType());
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

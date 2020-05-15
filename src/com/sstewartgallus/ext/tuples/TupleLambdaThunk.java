package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.ext.pretty.PrettyValue;
import com.sstewartgallus.ext.variables.VarValue;
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
    public Term<D> visitChildren(Visitor visitor) {
        throw new UnsupportedOperationException("Unimplemented");
    }

    @Override
    public Term<D> stepThunk() {
        return sig.stepThunk(f);
    }

    @Override
    public String toString() {
        return "(" + sig.stringify(f) + ")";
    }


    public interface Sig<T extends HList<T>, C, D> {

        Term<D> stepThunk(Function<T, Term<C>> f);

        Type<D> type();

        String stringify(Function<T, Term<C>> f);

        Results<?, C, D> uncurry(Function<T, Term<C>> f);

        record Zero<A>(Type<A>type) implements Sig<Nil, A, A> {
            @Override
            public Term<A> stepThunk(Function<Nil, Term<A>> f) {
                return f.apply(Nil.NIL);
            }

            @Override
            public String stringify(Function<Nil, Term<A>> f) {
                return ". → " + f.apply(Nil.NIL).toString();
            }

            @Override
            public Results<Nil, A, A> uncurry(Function<Nil, Term<A>> f) {
                var body = f.apply(Nil.NIL);
                return new Results<>(new UncurryLambdaThunk.Sig.Zero<>(type), nil -> body);
            }
        }

        record Cons<H, T extends HList<T>, C, D>(Type<H>head,
                                                 Sig<T, C, D>tail) implements Sig<com.sstewartgallus.ext.tuples.Cons<Term<H>, T>, C, F<H, D>> {

            private static <H, X extends HList<X>> Getter<X> nextGetter(Getter<com.sstewartgallus.ext.tuples.Cons<H, X>> product) {
                var get = (Getter.Get<?, com.sstewartgallus.ext.tuples.Cons<H, X>>) product;
                return nextGetter(get);
            }

            private static <T extends HList<T>, H, X extends HList<X>> Getter<X> nextGetter(Getter.Get<T, com.sstewartgallus.ext.tuples.Cons<H, X>> get) {
                var list = get.list();
                var index = new Index.Next<>(get.index());
                return new Getter.Get<>(list, index);
            }

            @Override
            public Term<F<H, D>> stepThunk(Function<com.sstewartgallus.ext.tuples.Cons<Term<H>, T>, Term<C>> f) {
                return head.l(h -> tail.stepThunk(t -> f.apply(new com.sstewartgallus.ext.tuples.Cons<>(h, t))));
            }

            @Override
            public Type<F<H, D>> type() {
                return head.to(tail.type());
            }

            @Override
            public String stringify(Function<com.sstewartgallus.ext.tuples.Cons<Term<H>, T>, Term<C>> f) {
                try (var pretty = PrettyValue.generate(head)) {
                    return "{" + pretty + ": " + head + "} Δ " + tail.stringify(t -> f.apply(new com.sstewartgallus.ext.tuples.Cons<>(pretty, t)));
                }
            }

            @Override
            public Results<?, C, F<H, D>> uncurry(Function<com.sstewartgallus.ext.tuples.Cons<Term<H>, T>, Term<C>> f) {
                var headVar = new VarValue<>(head);
                var value = tail.uncurry(t -> f.apply(new com.sstewartgallus.ext.tuples.Cons<>(headVar, t)));
                return cons(headVar, value);
            }

            public <X extends HList<X>> Results<com.sstewartgallus.ext.tuples.Cons<H, X>, C, F<H, D>> cons(VarValue<H> headId, Results<X, C, D> value) {
                var tailF = value.f();
                var sig = new UncurryLambdaThunk.Sig.Cons<>(head, value.sig());
                return new Results<>(sig, product -> {
                    Term<H> replacement = new DerefThunk<X, H>(product);
                    return headId.substituteIn(tailF.apply(nextGetter(product)), replacement);
                });
            }
        }
    }

    public record Results<L extends HList<L>, C, D>(
            com.sstewartgallus.ext.tuples.UncurryLambdaThunk.Sig<L, C, D>sig,
            Function<Getter<L>, Term<C>>f) {
        public Results {
            Objects.requireNonNull(sig);
            Objects.requireNonNull(f);
        }

        public UncurryLambdaThunk<L, C, D> toUncurry() {
            return new UncurryLambdaThunk<>(sig, x -> f.apply(new Getter.Get<>(x, new Index.Zip<>(x.type()))));
        }
    }

}

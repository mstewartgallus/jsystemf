package com.sstewartgallus.extensions.tuples;

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


    public interface Sig<T extends HList<T>, C, D> {

        Term<D> stepThunk(Function<T, Term<C>> f);

        Type<D> type();

        String stringify(Function<T, Term<C>> f, IdGen ids);

        Results<?, C, D> uncurry(Function<T, Term<C>> f, IdGen ids);

        record Zero<A>(Type<A>type) implements Sig<HList.Nil, A, A> {
            @Override
            public Term<A> stepThunk(Function<HList.Nil, Term<A>> f) {
                return f.apply(HList.Nil.NIL);
            }

            @Override
            public String stringify(Function<HList.Nil, Term<A>> f, IdGen ids) {
                return ". → " + f.apply(HList.Nil.NIL).toString();
            }

            @Override
            public Results<HList.Nil, A, A> uncurry(Function<HList.Nil, Term<A>> f, IdGen ids) {
                var body = f.apply(HList.Nil.NIL);
                return new Results<>(new UncurryLambdaThunk.Sig.Zero<>(type), nil -> body);
            }
        }

        record Cons<H, T extends HList<T>, C, D>(Type<H>head,
                                                 Sig<T, C, D>tail) implements Sig<HList.Cons<Term<H>, T>, C, F<H, D>> {

            private static <H, X extends HList<X>> Getter<X> nextGetter(Getter<HList.Cons<H, X>> product) {
                var get = (Getter.Get<?, HList.Cons<H, X>>) product;
                return nextGetter(get);
            }

            private static <T extends HList<T>, H, X extends HList<X>> Getter<X> nextGetter(Getter.Get<T, HList.Cons<H, X>> get) {
                var list = get.list();
                var index = new Index.Next<>(get.index());
                return new Getter.Get<>(list, index);
            }

            @Override
            public Term<F<H, D>> stepThunk(Function<HList.Cons<Term<H>, T>, Term<C>> f) {
                return head.l(h -> tail.stepThunk(t -> f.apply(new HList.Cons<>(h, t))));
            }

            @Override
            public Type<F<H, D>> type() {
                return head.to(tail.type());
            }

            @Override
            public String stringify(Function<HList.Cons<Term<H>, T>, Term<C>> f, IdGen ids) {
                var v = new VarValue<>(head, ids.createId());
                return "{" + v + ": " + head + "} Δ " + tail.stringify(t -> f.apply(new HList.Cons<>(v, t)), ids);
            }

            @Override
            public Results<?, C, F<H, D>> uncurry(Function<HList.Cons<Term<H>, T>, Term<C>> f, IdGen ids) {
                var headId = ids.<H>createId();
                var headVar = new VarValue<>(head, headId);
                var value = tail.uncurry(t -> f.apply(new HList.Cons<>(headVar, t)), ids);
                return cons(headId, value);
            }

            public <X extends HList<X>> Results<HList.Cons<H, X>, C, F<H, D>> cons(Id<H> headId, Results<X, C, D> value) {
                var tailF = value.f();
                var sig = new UncurryLambdaThunk.Sig.Cons<>(head, value.sig());
                return new Results<>(sig, product -> tailF.apply(nextGetter(product)).substitute(headId, new DerefThunk<X, H>(product)));
            }
        }
    }

    public record Results<L extends HList<L>, C, D>(
            com.sstewartgallus.extensions.tuples.UncurryLambdaThunk.Sig<L, C, D>sig,
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

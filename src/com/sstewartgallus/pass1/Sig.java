package com.sstewartgallus.pass1;

import com.sstewartgallus.plato.*;

import java.util.function.Function;

public interface Sig<T extends HList<T>, C, D> {

    Term<D> stepThunk(Function<T, Term<C>> f);

    Type<D> type();

    String stringify(Function<T, Term<C>> f, IdGen ids);

    record Zero<A>(Type<A>type) implements Sig<HList.Nil, A, A> {
        @Override
        public Term<A> stepThunk(Function<HList.Nil, Term<A>> f) {
            return f.apply(HList.Nil.NIL);
        }

        @Override
        public String stringify(Function<HList.Nil, Term<A>> f, IdGen ids) {
            return ". → " + f.apply(HList.Nil.NIL).toString();
        }
    }

    record Cons<H, T extends HList<T>, C, D>(Type<H>head,
                                             Sig<T, C, D>tail) implements Sig<HList.Cons<Term<H>, T>, C, F<H, D>> {
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

    }
}

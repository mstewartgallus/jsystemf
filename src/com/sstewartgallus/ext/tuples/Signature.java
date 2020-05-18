package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.plato.*;

public interface Signature<T extends Tuple<T>, C, D> {

    Term<D> stepThunk(Term<F<T, C>> f);

    Type<T> argType();

    Type<D> type();

    Type<C> retType();

    Term<C> stepThunkReverse(Term<D> apply, ValueTerm<T> t);

    Term<C> apply(Term<D> head, ValueTerm<T> tail);

    Term<D> curry(Term<F<T, C>> x);

    record Result<A>(Type<A>type) implements Signature<N, A, A> {
        @Override
        public Term<A> stepThunk(Term<F<N, A>> f) {
            return Term.apply(f, NilTupleValue.NIL);
        }


        @Override
        public Type<N> argType() {
            return NilTupleType.NIL;
        }

        @Override
        public Term<A> apply(Term<A> head, ValueTerm<N> tail) {
            return head;
        }

        @Override
        public Term<A> curry(Term<F<N, A>> x) {
            return Term.apply(x, NilTupleValue.NIL);
        }

        @Override
        public Type<A> retType() {
            return type;
        }

        @Override
        public Term<A> stepThunkReverse(Term<A> apply, ValueTerm<N> t) {
            return apply;
        }

        public String toString() {
            return " → " + type;
        }
    }

    record AddArg<H, T extends Tuple<T>, C, D>(Type<H>head,
                                               Signature<T, C, D>tail) implements Signature<P<H, T>, C, F<H, D>> {

        @Override
        public Term<F<H, D>> stepThunk(Term<F<P<H, T>, C>> f) {
            return head.l(h -> tail.stepThunk(tail.argType().l(t -> {
                var tNorm = Interpreter.normalize(t);
                return Term.apply(f, new TuplePairValue<>(h, tNorm));
            })));
        }

        @Override
        public Type<P<H, T>> argType() {
            return new TuplePairType<>(head, tail.argType());
        }

        @Override
        public Type<F<H, D>> type() {
            return head.to(tail.type());
        }

        @Override
        public Term<C> apply(Term<F<H, D>> head, ValueTerm<P<H, T>> list) {
            var p = (TuplePairValue<H, T>) list;
            var f = Term.apply(head, p.head());
            return tail.apply(f, p.tail());
        }

        @Override
        public Term<F<H, D>> curry(Term<F<P<H, T>, C>> f) {
            return head.l(h -> tail.curry(tail.argType().l(t -> Term.apply(f, new TuplePairValue<>(h, Interpreter.normalize(t))))));
        }

        @Override
        public Type<C> retType() {
            return tail.retType();
        }

        @Override
        public Term<C> stepThunkReverse(Term<F<H, D>> apply, ValueTerm<P<H, T>> list) {
            var listPair = (TuplePairValue<H, T>) list;

            return tail.stepThunkReverse(Term.apply(apply, listPair.head()), listPair.tail());
        }

        @Override
        public String toString() {
            StringBuilder str = new StringBuilder("((Δ " + head);
            Signature<?, ?, ?> current = tail;
            while (current instanceof Signature.AddArg<?, ?, ?, ?> addArg) {
                str.append(" ");
                str.append(addArg.head);
                current = addArg.tail;
            }
            str.append(") ");
            str.append(((Result<?>) current).type);
            str.append(")");
            return str.toString();
        }
    }

}

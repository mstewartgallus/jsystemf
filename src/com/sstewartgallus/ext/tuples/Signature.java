package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.plato.*;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;

public interface Signature<T extends Tuple<T>, C, D> {

    Term<D> stepThunk(Term<F<T, C>> f);

    Type<T> argType();

    Type<D> type();

    Type<C> retType();

    Term<C> stepThunkReverse(Term<D> apply, ValueTerm<T> t);

    Term<F<T, C>> stepThunkReverse(Term<D> fTerm);

    Term<D> stepThunk(MethodHandle methodHandle);

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
        public Term<A> stepThunk(MethodHandle methodHandleThunk) {
            throw null;
        }

        @Override
        public Term<F<N, A>> stepThunkReverse(Term<A> term) {
            return NilTupleType.NIL.l(n -> term);
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
        private static List<Term<?>> toList(Term<?> x) {
            var list = new ArrayList<Term<?>>();

            var node = Interpreter.normalize(x);
            while (node instanceof TuplePairValue<?, ?> cons) {
                list.add(cons.head());
                node = Interpreter.normalize(cons.tail());
            }
            return list;
        }

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
        public Term<F<H, D>> stepThunk(MethodHandle handle) {
            return head.l(x -> {
                // fixme... we need a generic adapter procedure/hopefully at least slightly faster...
                var list = toList(x);

                Term<D> result;
                try {
                    result = (Term) handle.invokeWithArguments(list);
                } catch (Error | RuntimeException e) {
                    throw e;
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
                return result;
            });
        }

        @Override
        public Term<F<P<H, T>, C>> stepThunkReverse(Term<F<H, D>> term) {
            return new TuplePairType<>(head, tail.argType()).l(x -> {
                var xNorm = (TuplePairValue<H, T>) Interpreter.normalize(x);
                var h = xNorm.head();
                var t = xNorm.tail();
                // tuples are NOT lazy
                return tail.stepThunkReverse(Term.apply(term, h), t);
            });
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

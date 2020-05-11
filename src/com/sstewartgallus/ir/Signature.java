package com.sstewartgallus.ir;

import com.sstewartgallus.pass1.TPass0;
import com.sstewartgallus.type.E;
import com.sstewartgallus.type.F;
import com.sstewartgallus.type.HList;
import com.sstewartgallus.type.V;

// fixme... simplify point-free type representation...
public interface Signature<A, B> {
    static <B, A, T> Signature<T, V<A, B>> curry(Signature<E<A, T>, B> body) {
        return new Signature.Curry<>(body);
    }

    // fixme... probably going to need my own runtime of type values... my current type is more like ClassDesc than class
    default TPass0<B> apply(TPass0<A> input) {
        throw new UnsupportedOperationException(getClass().toString());
    }

    record Pure<T, A>(Class<A>clazz) implements Signature<T, A> {
        public TPass0<A> apply(TPass0<T> input) {
            return new TPass0.PureType<>(clazz);
        }

        public String toString() {
            return clazz.getName();
        }
    }

    record Function<X, A, B>(Signature<X, A>domain, Signature<X, B>range) implements Signature<X, F<A, B>> {
        public TPass0<F<A, B>> apply(TPass0<X> input) {
            return new TPass0.FunType<>(domain.apply(input), range.apply(input));
        }

        public String toString() {
            return domain + " → " + range;
        }
    }

    record First<L, A, B>(Signature<L, E<A, B>>sig) implements Signature<L, A> {
        public TPass0<A> apply(TPass0<L> input) {
            return ((TPass0.Exists<A, B>) sig.apply(input)).x();
        }

        public String toString() {
            return "(exl " + sig + ")";
        }
    }

    record Second<L, A, B>(Signature<L, E<A, B>>sig) implements Signature<L, B> {
        public TPass0<B> apply(TPass0<L> input) {
            return ((TPass0.Exists<A, B>) sig.apply(input)).y();
        }

        public String toString() {
            return "(exr " + sig + ")";
        }
    }

    record Identity<T>() implements Signature<T, T> {
        public TPass0<T> apply(TPass0<T> input) {
            return input;
        }

        public String toString() {
            return "id";
        }
    }

    record Curry<A, B, C>(Signature<E<A, C>, B>body) implements Signature<C, V<A, B>> {
        public TPass0<V<A, B>> apply(TPass0<C> input) {
            throw new UnsupportedOperationException("unimplemented");
        }

        public String toString() {
            return "(curry " + body + ")";
        }
    }

    record NilTPass0<X>() implements Signature<X, HList.Nil> {
        public TPass0<HList.Nil> apply(TPass0<X> input) {
            return TPass0.NilType.NIL;
        }

        public String toString() {
            return "[]";
        }

    }

    record ConsTPass0<X, H, T extends HList<T>>(Signature<X, H>head,
                                                Signature<X, T>tail) implements Signature<X, HList.Cons<H, T>> {
        public TPass0<HList.Cons<H, T>> apply(TPass0<X> input) {
            return new TPass0.ConsType<>(head.apply(input), tail.apply(input));
        }


        public String toString() {
            var builder = new StringBuilder();
            builder.append("(");
            builder.append(head);

            Signature<?, ? extends HList<?>> current = tail;
            while (current instanceof ConsTPass0<?, ?, ?> cons) {
                builder.append(" Δ ");
                builder.append(cons.head);
                current = cons.tail;
            }
            builder.append(" Δ .)");
            return builder.toString();
        }
    }
}

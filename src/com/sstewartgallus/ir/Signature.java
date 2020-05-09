package com.sstewartgallus.ir;

import com.sstewartgallus.type.*;

public interface Signature<A, B> {
    static <B, A, T> Signature<T, V<A, B>> curry(Signature<E<A, T>, B> body) {
        return new Signature.Curry<>(body);
    }

    // fixme... probably going to need my own runtime of type values... my current type is more like ClassDesc than class
    default Type<B> apply(Type<A> input) {
        throw new UnsupportedOperationException(getClass().toString());
    }

    default <C> Signature<A, F<A, C>> compose(Signature<A, F<A, B>> signature) {
        throw new UnsupportedOperationException("unimplemented");
    }

    record Pure<T, A>(Class<A>clazz) implements Signature<T, A> {
        public Type<A> apply(Type<T> input) {
            return new Type.PureType<>(clazz);
        }

        public String toString() {
            return clazz.getName();
        }
    }

    record Function<X, A, B>(Signature<X, A>domain, Signature<X, B>range) implements Signature<X, F<A, B>> {
        public Type<F<A, B>> apply(Type<X> input) {
            return domain.apply(input).to(range.apply(input));
        }

        public String toString() {
            return domain + " -> " + range;
        }
    }

    record First<L, A, B>(Signature<L, E<A, B>>sig) implements Signature<L, A> {
        public Type<A> apply(Type<L> input) {
            return ((Type.Exists<A, B>) sig.apply(input)).x();
        }

        public String toString() {
            return "(exl " + sig + ")";
        }
    }

    record Second<L, A, B>(Signature<L, E<A, B>>sig) implements Signature<L, B> {
        public Type<B> apply(Type<L> input) {
            return ((Type.Exists<A, B>) sig.apply(input)).y();
        }

        public String toString() {
            return "(exr " + sig + ")";
        }
    }

    record Identity<T>() implements Signature<T, T> {
        public Type<T> apply(Type<T> input) {
            return input;
        }

        public String toString() {
            return "id";
        }
    }

    record Curry<A, B, C>(Signature<E<A, C>, B>body) implements Signature<C, V<A, B>> {
        public Type<V<A, B>> apply(Type<C> input) {
            throw new UnsupportedOperationException("unimplemented");
        }

        public String toString() {
            return "(curry " + body + ")";
        }
    }

    record NilType<X>() implements Signature<X, Nil> {
        public Type<Nil> apply(Type<X> input) {
            return Type.nil();
        }

        public String toString() {
            return "[]";
        }

    }

    record ConsType<X, H, T extends HList>(Signature<X, H>head,
                                           Signature<X, T>tail) implements Signature<X, Cons<H, T>> {
        public Type<Cons<H, T>> apply(Type<X> input) {
            return Type.cons(head.apply(input), tail.apply(input));
        }

        public String toString() {
            return "(" + head + " : " + tail + ")";
        }
    }
}

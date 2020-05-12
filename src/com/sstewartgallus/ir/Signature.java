package com.sstewartgallus.ir;

import com.sstewartgallus.type.F;
import com.sstewartgallus.type.HList;
import com.sstewartgallus.type.V;

import java.util.ArrayList;
import java.util.List;

public interface Signature<A> {
    // fixme... probably going to need my own runtime of type values... my current type is more like ClassDesc than class
    static <A, B> Signature<B> apply(Signature<V<A, B>> f, Signature<A> x) {
        return ((SigV<A, B>) f).apply(x);
    }

    Class<?> erase();

    default List<Class<?>> flatten() {
        return List.of(erase());
    }

    enum NilSig implements Signature<HList.Nil> {
        NIL;

        @Override
        public Class<?> erase() {
            return Void.class;
        }
    }

    record K<T, A>(Signature<A>value) implements SigV<T, A> {
        public Signature<A> apply(Signature<T> input) {
            return value;
        }

        public String toString() {
            return "(K " + value + ")";
        }

        @Override
        public Class<?> erase() {
            throw new UnsupportedOperationException("unimplemented");
        }
    }

    record Pure<T, A>(Class<A>clazz) implements Signature<A> {
        public String toString() {
            return clazz.getName();
        }

        @Override
        public Class<?> erase() {
            return clazz;
        }
    }

    record Function<X, A, B>(Signature<V<X, A>>domain, Signature<V<X, B>>range) implements SigV<X, F<A, B>> {
        public Signature<F<A, B>> apply(Signature<X> x) {
            return new FunctionGround<>(Signature.apply(domain, x), Signature.apply(range, x));
        }

        public String toString() {
            return domain + " → " + range;
        }

        @Override
        public Class<?> erase() {
            throw null;
        }
    }

    record FunctionGround<A, B>(Signature<A>domain, Signature<B>range) implements Signature<F<A, B>> {
        public String toString() {
            return domain + " → " + range;
        }

        @Override
        public Class<?> erase() {
            return Object.class;
        }
    }

    record Identity<T>() implements SigV<T, T> {
        public Signature<T> apply(Signature<T> input) {
            return input;
        }

        public String toString() {
            return "I";
        }

        @Override
        public Class<?> erase() {
            throw new UnsupportedOperationException("unimplemented");
        }
    }

    record ConsType<X, H, T extends HList<T>>(Signature<V<X, H>>head,
                                              Signature<V<X, T>>tail) implements SigV<X, HList.Cons<H, T>> {
        public Signature<HList.Cons<H, T>> apply(Signature<X> input) {
            return new Signature.ConsTypeGround<>(Signature.apply(head, input), Signature.apply(tail, input));
        }

        public String toString() {
            var builder = new StringBuilder();
            builder.append("(");
            builder.append(head);

            Signature<?> current = tail;
            while (current instanceof ConsType<?, ?, ?> cons) {
                builder.append(" Δ ");
                builder.append(cons.head);
                current = cons.tail;
            }
            builder.append(" Δ .)");
            return builder.toString();
        }

        @Override
        public Class<?> erase() {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public List<Class<?>> flatten() {
            throw new UnsupportedOperationException("unimplemented");

        }
    }

    record ConsTypeGround<H, T extends HList<T>>(Signature<H>head,
                                                 Signature<T>tail) implements Signature<HList.Cons<H, T>> {

        public String toString() {
            var builder = new StringBuilder();
            builder.append("(");
            builder.append(head);

            Signature<?> current = tail;
            while (current instanceof ConsTypeGround<?, ?> cons) {
                builder.append(" Δ ");
                builder.append(cons.head);
                current = cons.tail;
            }
            builder.append(" Δ .)");
            return builder.toString();
        }

        @Override
        public Class<?> erase() {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public List<Class<?>> flatten() {
            var l = new ArrayList<Class<?>>();
            l.add(head.erase());

            Signature<?> current = tail;
            while (current instanceof Signature.ConsTypeGround<?, ?> cons) {
                l.add(cons.head.erase());
                current = cons.tail;
            }
            return l;
        }
    }
}

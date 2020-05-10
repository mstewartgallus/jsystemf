package com.sstewartgallus.type;

import com.sstewartgallus.ir.Signature;
import com.sstewartgallus.ir.TVarGen;
import com.sstewartgallus.runtime.Closure;
import com.sstewartgallus.runtime.ConsValue;
import com.sstewartgallus.term.Term;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicConstantDesc;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public interface Type<X> {
    Type<Integer> INT = new PureType<>(int.class);
    Type<Boolean> BOOLEAN = new PureType<>(boolean.class);
    Type<Void> VOID = new PureType<>(Void.class);

    static <A, B> Type<E<A, B>> e(Type<A> x, Type<B> y) {
        return new Exists<>(x, y);
    }

    static <A, B> Type<V<A, B>> v(Function<Type<A>, Type<B>> f) {
        return new Forall<>(f);
    }

    static Type<HList.Nil> nil() {
        return NilType.NIL;
    }

    static <H, T extends HList<T>> Type<HList.Cons<H, T>> cons(Type<H> head, Type<T> tail) {
        return new ConsType<>(head, tail);
    }

    default <B> Term<F<X, B>> l(Function<Term<X>, Term<B>> f) {
        return new Term.Lambda<>(this, f);
    }

    default <B> Type<F<X, B>> to(Type<B> range) {
        return new FunType<>(this, range);
    }

    default List<Class<?>> flatten() {
        return List.of(erase());
    }

    default Class<?> erase() {
        throw new UnsupportedOperationException(getClass().toString());
    }

    default <A> Signature<A, X> ccc() {
        var vars = new TVarGen();
        return ccc(vars.createTypeVar(), vars);
    }

    default <A> Signature<A, X> ccc(Var<A> v, TVarGen vars) {
        throw new UnsupportedOperationException(getClass().toString());
    }

    default <T> Type<X> substitute(Var<T> v, Type<T> replacement) {
        throw new UnsupportedOperationException(getClass().toString());
    }

    enum NilType implements Type<HList.Nil> {
        NIL;

        public List<Class<?>> flatten() {
            return List.of();
        }

        public <X> Signature<X, HList.Nil> ccc(Var<X> v, TVarGen vars) {
            return new Signature.NilType<>();
        }

        public Class<?> erase() {
            // fixme... should be possible to flatten
            return ConsValue.class;
        }
    }

    record FunType<A, B>(Type<A>domain, Type<B>range) implements Type<F<A, B>> {
        public <X> Signature<X, F<A, B>> ccc(Var<X> v, TVarGen vars) {
            return new Signature.Function<>(domain.ccc(v, vars), range.ccc(v, vars));
        }

        public <Z> Type<F<A, B>> substitute(Var<Z> v, Type<Z> replacement) {
            return new FunType<>(domain.substitute(v, replacement), range.substitute(v, replacement));
        }

        // we use a generic protocol for our functions
        public Class<?> erase() {
            return Closure.class;
        }

        public String toString() {
            return "{" + domain + " → " + range + "}";
        }
    }

    record PureType<A>(Class<A>clazz) implements Type<A> {
        public <T> Signature<T, A> ccc(Var<T> argument, TVarGen vars) {
            return new Signature.Pure<>(clazz);
        }

        public <Z> Type<A> substitute(Var<Z> v, Type<Z> replacement) {
            return new PureType<>(clazz);
        }

        @Override
        public Class<?> erase() {
            return clazz;
        }

        public String toString() {
            return erase().getCanonicalName();
        }
    }

    record First<A, B>(Type<E<A, B>>value) implements Type<A> {
        public <L> Signature<L, A> ccc(Var<L> argument, TVarGen vars) {

            return new Signature.First<>(value.ccc(argument, vars));
        }

        public <Z> Type<A> substitute(Var<Z> v, Type<Z> replacement) {
            return new First<>(value.substitute(v, replacement));
        }
    }

    record Second<A, B>(Type<E<A, B>>value) implements Type<B> {
        public <L> Signature<L, B> ccc(Var<L> argument, TVarGen vars) {
            return new Signature.Second<>(value.ccc(argument, vars));
        }

        public <Z> Type<B> substitute(Var<Z> v, Type<Z> replacement) {
            return new Second<>(value.substitute(v, replacement));
        }
    }

    record Forall<A, B>(Function<Type<A>, Type<B>>f) implements Type<com.sstewartgallus.type.V<A, B>> {
        private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

        public <T> Signature<T, V<A, B>> ccc(Var<T> argument, TVarGen vars) {
            Type.Var<E<A, T>> newVar = vars.createTypeVar();
            System.err.println(argument + " " + newVar);

            var body = f.apply(new First<>(newVar))
                    .substitute(argument, new Second<>(newVar))
                    .ccc(newVar, vars);
            return Signature.curry(body);
        }

        public String toString() {
            var depth = DEPTH.get();
            DEPTH.set(depth + 1);

            String str;
            try {
                var t = new Var<A>(depth);
                str = "{forall " + t + ". " + f.apply(t) + "}";
            } finally {
                DEPTH.set(depth);
                if (depth == 0) {
                    DEPTH.remove();
                }
            }
            return str;
        }
    }

    record Exists<A, B>(Type<A>x, Type<B>y) implements Type<com.sstewartgallus.type.E<A, B>> {
        public String toString() {
            return "{exists " + x + ". " + y + "}";
        }
    }

    record Var<T>(int depth) implements Type<T> {
        @Override
        public String toString() {
            return "t" + depth;
        }

        public <V> Signature<V, T> ccc(Var<V> argument, TVarGen vars) {
            if (argument == this) {
                return (Signature<V, T>) new Signature.Identity<T>();
            }
            throw new IllegalStateException("wrong variable " + argument + " " + this);
        }


        public <Z> Type<T> substitute(Var<Z> v, Type<Z> replacement) {
            if (v == this) {
                return (Type<T>) replacement;
            }
            return this;
        }
    }

    final class TypeDesc<A> extends DynamicConstantDesc<Type<A>> {
        private TypeDesc(DirectMethodHandleDesc bootstrapMethod, String constantName, ClassDesc constantType, ConstantDesc... bootstrapArgs) {
            super(bootstrapMethod, constantName, constantType, bootstrapArgs);
        }
    }

    record ConsType<H, T extends HList<T>>(Type<H>head, Type<T>tail) implements Type<HList.Cons<H, T>> {
        public Class<?> erase() {
            // fixme... should be possible to flatten
            return ConsValue.class;
        }

        public List<Class<?>> flatten() {
            var l = new ArrayList<>(tail.flatten());
            l.add(head.erase());
            return l;
        }

        public <X> Signature<X, HList.Cons<H, T>> ccc(Var<X> v, TVarGen vars) {
            return new Signature.ConsType<>(head.ccc(v, vars), tail.ccc(v, vars));
        }

        public String toString() {
            var builder = new StringBuilder();
            builder.append("(");
            builder.append(head);

            Type<? extends HList<?>> current = tail;
            while (current instanceof ConsType<?, ?> cons) {
                builder.append(" Δ ");
                builder.append(cons.head);
                current = cons.tail;
            }
            builder.append(" Δ .)");
            return builder.toString();
        }
    }
}

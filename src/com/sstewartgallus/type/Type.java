package com.sstewartgallus.type;

import com.sstewartgallus.ir.Signature;
import com.sstewartgallus.ir.TVarGen;
import com.sstewartgallus.runtime.FunValue;
import com.sstewartgallus.term.Term;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

// fixme... get rid of compiler ir hackery...
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

    // fixme... rethink unification...
    default <Y> Type<X> unify(Type<Y> right) throws TypeCheckException {
        throw null;
    }

    default <B> Term<F<X, B>> l(Function<Term<X>, Term<B>> f) {
        return new Term.Lambda<>(this, f);
    }

    default <B> Type<F<X, B>> to(Type<B> range) {
        return new FunType<>(this, range);
    }

    // fixme... compiler IR nonsense...
    default List<Class<?>> flatten() {
        return List.of(erase());
    }

    default Class<?> erase() {
        throw new UnsupportedOperationException(getClass().toString());
    }

    default <A> Signature<A, X> pointFree(Var<A> v, TVarGen vars) {
        throw new UnsupportedOperationException(getClass().toString());
    }

    default <T> Type<X> substitute(Var<T> v, Type<T> replacement) {
        throw new UnsupportedOperationException(getClass().toString());
    }

    enum NilType implements Type<HList.Nil> {
        NIL;

        public <Y> Type<HList.Nil> unify(Type<Y> right) throws TypeCheckException {
            if (this != right) {
                throw new TypeCheckException(this, right);
            }
            return this;
        }

        @Override
        public List<Class<?>> flatten() {
            return List.of(Void.class);
        }

        @Override
        public <X> Signature<X, HList.Nil> pointFree(Var<X> v, TVarGen vars) {
            return new Signature.NilType<>();
        }

        @Override
        public Class<?> erase() {
            throw new UnsupportedOperationException("unimplemented");
        }
    }

    record FunType<A, B>(Type<A>domain, Type<B>range) implements Type<F<A, B>> {
        @Override
        public <Y> Type<F<A, B>> unify(Type<Y> right) throws TypeCheckException {
            if (!(right instanceof FunType<?, ?> funType)) {
                throw new TypeCheckException(this, right);
            }
            return new FunType<>(domain.unify(funType.domain), range.unify(funType.range));
        }

        @Override
        public <X> Signature<X, F<A, B>> pointFree(Var<X> v, TVarGen vars) {
            return new Signature.Function<>(domain.pointFree(v, vars), range.pointFree(v, vars));
        }

        @Override
        public <Z> Type<F<A, B>> substitute(Var<Z> v, Type<Z> replacement) {
            return new FunType<>(domain.substitute(v, replacement), range.substitute(v, replacement));
        }

        // fixme... consider just using object as we use a generic protocol for our functions
        @Override
        public Class<?> erase() {
            return FunValue.class;
        }

        @Override
        public String toString() {
            return "{" + domain + " → " + range + "}";
        }
    }

    // fixme... rename/retype, not clear enough this creates a new type...
    record PureType<A>(Class<A>clazz) implements Type<A> {
        public <Y> Type<A> unify(Type<Y> right) throws TypeCheckException {
            if (this != right) {
                throw new TypeCheckException(this, right);
            }
            return this;
        }

        public <T> Signature<T, A> pointFree(Var<T> argument, TVarGen vars) {
            return new Signature.Pure<>(clazz);
        }

        public <Z> Type<A> substitute(Var<Z> v, Type<Z> replacement) {
            return new PureType<>(clazz);
        }

        @Override
        public Class<?> erase() {
            return clazz;
        }

        @Override
        public String toString() {
            return erase().getCanonicalName();
        }
    }

    record First<A, B>(Type<E<A, B>>value) implements Type<A> {
        public <L> Signature<L, A> pointFree(Var<L> argument, TVarGen vars) {
            return new Signature.First<>(value.pointFree(argument, vars));
        }

        public <Z> Type<A> substitute(Var<Z> v, Type<Z> replacement) {
            return new First<>(value.substitute(v, replacement));
        }
    }

    record Second<A, B>(Type<E<A, B>>value) implements Type<B> {
        public <L> Signature<L, B> pointFree(Var<L> argument, TVarGen vars) {
            return new Signature.Second<>(value.pointFree(argument, vars));
        }

        public <Z> Type<B> substitute(Var<Z> v, Type<Z> replacement) {
            return new Second<>(value.substitute(v, replacement));
        }
    }

    record Forall<A, B>(Function<Type<A>, Type<B>>f) implements Type<V<A, B>> {
        private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

        public <T> Signature<T, V<A, B>> pointFree(Var<T> argument, TVarGen vars) {
            Type.Var<E<A, T>> newVar = vars.createTypeVar();

            var body = f.apply(new First<>(newVar))
                    .substitute(argument, new Second<>(newVar))
                    .pointFree(newVar, vars);
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

    record Exists<A, B>(Type<A>x, Type<B>y) implements Type<E<A, B>> {
        @Override
        public String toString() {
            return "{exists " + x + ". " + y + "}";
        }
    }

    record Var<T>(int depth) implements Type<T> {
        @Override
        public String toString() {
            return "domain" + depth;
        }

        @Override
        public <V> Signature<V, T> pointFree(Var<V> argument, TVarGen vars) {
            if (argument == this) {
                return (Signature<V, T>) new Signature.Identity<T>();
            }
            throw new IllegalStateException("wrong variable " + argument + " " + this);
        }

        @Override
        public <Z> Type<T> substitute(Var<Z> v, Type<Z> replacement) {
            if (v == this) {
                return (Type<T>) replacement;
            }
            return this;
        }
    }

    record ConsType<H, T extends HList<T>>(Type<H>head, Type<T>tail) implements Type<HList.Cons<H, T>> {
        @Override
        public Class<?> erase() {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public List<Class<?>> flatten() {
            var l = new ArrayList<Class<?>>();
            l.add(head.erase());

            Type<?> current = tail;
            while (current instanceof ConsType<?, ?> cons) {
                l.add(cons.head.erase());
                current = cons.tail;
            }
            return l;
        }

        @Override
        public <X> Signature<X, HList.Cons<H, T>> pointFree(Var<X> v, TVarGen vars) {
            return new Signature.ConsType<>(head.pointFree(v, vars), tail.pointFree(v, vars));
        }

        @Override
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

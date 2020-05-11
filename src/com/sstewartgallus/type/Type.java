package com.sstewartgallus.type;

import com.sstewartgallus.term.Term;

import java.util.function.Function;

/**
 * This is intended to be pristine source language untainted by compiler stuff.
 * <p>
 * Any processing should happen AFTER this step.
 */
public interface Type<X> {
    Type<Integer> INT = new PureType<>(int.class);

    static <A, B> Type<E<A, B>> e(Type<A> x, Type<B> y) {
        return new Exists<>(x, y);
    }

    static <A, B> Type<V<A, B>> v(Function<Type<A>, Type<B>> f) {
        return new Forall<>(f);
    }

    // fixme... rethink unification...

    <Y> Type<X> unify(Type<Y> right) throws TypeCheckException;

    default <B> Term<F<X, B>> l(Function<Term<X>, Term<B>> f) {
        return new Term.Lambda<>(this, f);
    }

    default <B> Type<F<X, B>> to(Type<B> range) {
        return new FunType<>(this, range);
    }

    <L> L visit(Visitor<L, X> visitor);

    <T> Type<X> substitute(TVar<T> v, Type<T> replacement);

    interface Visitor<X, L> {
        X onPureType(Class<L> clazz);

        X onLoadType(TVar<L> variable);

        <A, B> X onFunctionType(Equality<L, F<A, B>> equality, Type<A> domain, Type<B> range);
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
        public <L> L visit(Visitor<L, F<A, B>> visitor) {
            return visitor.onFunctionType(new Equality.Identical<>(), domain, range);
        }

        @Override
        public <Z> Type<F<A, B>> substitute(TVar<Z> v, Type<Z> replacement) {
            return new FunType<>(domain.substitute(v, replacement), range.substitute(v, replacement));
        }

        @Override
        public String toString() {
            return "{" + domain + " → " + range + "}";
        }
    }

    // fixme... rename/retype, not clear enough this creates a new type...
    record PureType<A>(Class<A>clazz) implements Type<A> {
        @Override
        public <L> L visit(Visitor<L, A> visitor) {
            return visitor.onPureType(clazz);
        }

        public <Y> Type<A> unify(Type<Y> right) throws TypeCheckException {
            if (this != right) {
                throw new TypeCheckException(this, right);
            }
            return this;
        }

        @Override
        public <Z> Type<A> substitute(TVar<Z> v, Type<Z> replacement) {
            return new PureType<>(clazz);
        }

        @Override
        public String toString() {
            return clazz.getCanonicalName();
        }
    }

    record Forall<A, B>(Function<Type<A>, Type<B>>f) implements Type<V<A, B>> {
        private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

        public String toString() {
            var depth = DEPTH.get();
            DEPTH.set(depth + 1);

            String str;
            try {
                var t = new TVar<A>(depth);
                str = "{forall " + t + ". " + f.apply(new Load<>(t)) + "}";
            } finally {
                DEPTH.set(depth);
                if (depth == 0) {
                    DEPTH.remove();
                }
            }
            return str;
        }

        @Override
        public <Y> Type<V<A, B>> unify(Type<Y> right) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public <L> L visit(Visitor<L, V<A, B>> visitor) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public <T> Type<V<A, B>> substitute(TVar<T> v, Type<T> replacement) {
            throw new UnsupportedOperationException("unimplemented");
        }
    }

    record Exists<A, B>(Type<A>x, Type<B>y) implements Type<E<A, B>> {
        @Override
        public String toString() {
            return "{exists " + x + ". " + y + "}";
        }

        @Override
        public <Y> Type<E<A, B>> unify(Type<Y> right) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public <L> L visit(Visitor<L, E<A, B>> visitor) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public <T> Type<E<A, B>> substitute(TVar<T> v, Type<T> replacement) {
            throw new UnsupportedOperationException("unimplemented");
        }
    }

    record Load<T>(TVar<T>variable) implements Type<T> {
        @Override
        public String toString() {
            return variable.toString();
        }

        @Override
        public <Z> Type<T> substitute(TVar<Z> v, Type<Z> replacement) {
            if (v == variable) {
                return (Type<T>) replacement;
            }
            return this;
        }

        @Override
        public <Y> Type<T> unify(Type<Y> right) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public <L> L visit(Visitor<L, T> visitor) {
            return visitor.onLoadType(variable);
        }
    }
}

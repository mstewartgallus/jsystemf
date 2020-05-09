package com.sstewartgallus.ir;

import com.sstewartgallus.pass1.Args;
import com.sstewartgallus.type.*;

import java.lang.constant.ConstantDesc;
import java.util.function.Function;

/**
 * compile to a closed cartesian category
 * <p>
 * really for full enterprise javaness it should be called AbstractCategory but whatever....
 */
public interface Category<A, B> {

    static <A> Generic<Void, F<Nil, A>> generic(Category<Nil, A> category) {
        var vars = new TVarGen();
        return category.generic(vars.createTypeVar(), vars);
    }

    static <X, A, B extends HList> Category<X, A> head(Category<X, Cons<A, B>> product) {
        return new Head<>(product);
    }

    static <X, A, B extends HList> Category<X, B> tail(Category<X, Cons<A, B>> product) {
        return new Tail<>(product);
    }

    static <V, A, B> Category<V, B> call(Category<V, F<A, B>> f, Category<V, A> x) {
        return new Call<>(f, x);
    }

    static <B, A> Category<B, A> constant(Type<B> domain, Type<A> range, ConstantDesc value) {
        return new Unit<>(domain, range, value);
    }

    static <A extends HList, R, B, Z> Category<Z, R> makeLambda(Type<Z> domain, Type<R> range, Args<A, B, R> arguments, Category<A, B> ccc) {
        return new MakeLambda<>(domain, range, arguments, ccc);
    }

    static <A, B, T extends HList> Category<T, F<A, B>> curry(Category<Cons<A, T>, B> ccc) {
        return new Curry<>(ccc);
    }

    // fixme.. use separate TypeVarGen type
    <Z> Generic<Z, F<A, B>> generic(Type.Var<Z> argument, TVarGen vars);

    <Z> Category<A, B> substitute(Type.Var<Z> argument, Type<Z> replacement);

    Type<A> domain();

    Type<B> range();

    record Unit<A, B>(Type<A>domain, Type<B>range, ConstantDesc value) implements Category<A, B> {
        public String toString() {
            return "(K " + value.toString() + ")";
        }

        public <V> Generic<V, F<A, B>> generic(Type.Var<V> argument, TVarGen vars) {
            var sig = domain.to(range).ccc(argument, vars);
            return new Generic.Unit<V, A, B>(sig, domain.ccc(argument, vars), range.ccc(argument, vars), value);
        }

        public <V> Category<A, B> substitute(Type.Var<V> argument, Type<V> replacement) {
            return new Unit<>(domain.substitute(argument, replacement), range.substitute(argument, replacement), value);
        }
    }

    record Identity<A>(Type<A>type) implements Category<A, A> {

        public <V> Generic<V, F<A, A>> generic(Type.Var<V> argument, TVarGen vars) {
            var sig = new Type.FunType<>(domain(), range()).ccc(argument, vars);
            return new Generic.Identity<>(sig, type.ccc(argument, vars));
        }

        public <V> Category<A, A> substitute(Type.Var<V> argument, Type<V> replacement) {
            return new Identity<>(type.substitute(argument, replacement));
        }

        @Override
        public Type<A> domain() {
            return type;
        }

        @Override
        public Type<A> range() {
            return type;
        }

        public String toString() {
            return "I";
        }
    }

    // fixme... get type of the pair we are using..
    record Head<X, A, B extends HList>(Category<X, Cons<A, B>>product) implements Category<X, A> {
        public <V> Generic<V, F<X, A>> generic(Type.Var<V> argument, TVarGen vars) {
            return new Generic.Head<V, X, A, B>(domain().to(range()).ccc(argument, vars),
                    range().ccc(argument, vars),
                    product.generic(argument, vars));
        }

        public <V> Category<X, A> substitute(Type.Var<V> argument, Type<V> replacement) {
            return new Head<>(product.substitute(argument, replacement));
        }

        public String toString() {
            return "(head " + product + ")";
        }

        @Override
        public Type<X> domain() {
            return product.domain();
        }

        @Override
        public Type<A> range() {
            return ((Type.ConsType<A, B>) product.range()).head();
        }
    }

    record Tail<X, A, B extends HList>(Category<X, Cons<A, B>>product) implements Category<X, B> {
        public <V> Generic<V, F<X, B>> generic(Type.Var<V> argument, TVarGen vars) {
            return new Generic.Tail<V, X, A, B>(domain().to(range()).ccc(argument, vars),
                    range().ccc(argument, vars),
                    product.generic(argument, vars));
        }

        public <V> Category<X, B> substitute(Type.Var<V> argument, Type<V> replacement) {
            return new Tail<>(product.substitute(argument, replacement));
        }

        @Override
        public Type<X> domain() {
            return product.domain();
        }

        @Override
        public Type<B> range() {
            return ((Type.ConsType<A, B>) product.range()).tail();
        }

        public String toString() {
            return "(tail " + product + ")";
        }
    }

    record Curry<A, B extends HList, C>(Category<Cons<A, B>, C>f) implements Category<B, F<A, C>> {
        public <V> Generic<V, F<B, F<A, C>>> generic(Type.Var<V> argument, TVarGen vars) {
            var prod = ((Type.ConsType<A, B>) (f.domain()));
            return new Generic.Curry<>(domain().to(range()).ccc(argument, vars),
                    prod.head().ccc(argument, vars),
                    prod.tail().ccc(argument, vars),
                    f.generic(argument, vars));
        }

        public <V> Category<B, F<A, C>> substitute(Type.Var<V> argument, Type<V> replacement) {
            return new Curry<>(f.substitute(argument, replacement));
        }

        @Override
        public Type<B> domain() {
            return ((Type.ConsType<A, B>) (f.domain())).tail();
        }

        @Override
        public Type<F<A, C>> range() {
            return second().to(f.range());
        }

        public Type<A> second() {
            return ((Type.ConsType<A, B>) (f.domain())).head();
        }

        public String toString() {
            return "(curry " + f + ")";
        }
    }

    record Exists<X, A, B>(Type<A>x, Category<X, B>y) implements Category<X, E<A, B>> {

        @Override
        public <Z> Generic<Z, F<X, E<A, B>>> generic(Type.Var<Z> argument, TVarGen vars) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public <Z> Category<X, E<A, B>> substitute(Type.Var<Z> argument, Type<Z> replacement) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public Type<X> domain() {
            return y.domain();
        }

        @Override
        public Type<E<A, B>> range() {
            return new Type.Exists<>(x, y.range());
        }
    }

    record Forall<X, A, B>(Type<X>domain, Function<Type<A>, Category<X, B>>f) implements Category<X, V<A, B>> {
        public <Z> Generic<Z, F<X, V<A, B>>> generic(Type.Var<Z> argument, TVarGen vars) {
            Type.Var<E<Z, A>> arg = vars.createTypeVar();

            Signature<Z, F<X, V<A, B>>> signature = domain().to(range()).ccc(argument, vars);

            Generic<E<Z, A>, F<X, B>> body = f.apply(new Type.Second<>(arg))
                    .substitute(argument, new Type.First<>(arg))
                    .generic(arg, vars);
            return Generic.curry(signature, body);
        }

        public <Z> Category<X, V<A, B>> substitute(Type.Var<Z> argument, Type<Z> replacement) {
            return new Forall<>(domain, arg -> f.apply(arg).substitute(argument, replacement));

        }

        @Override
        public Type<V<A, B>> range() {
            return new Type.Forall<>(arg -> f.apply(arg).range());
        }
    }

    record Call<Z, A, B>(Category<Z, F<A, B>>f, Category<Z, A>x) implements Category<Z, B> {
        @Override
        public <V> Generic<V, F<Z, B>> generic(Type.Var<V> argument, TVarGen vars) {
            var sig = domain().to(range()).ccc(argument, vars);
            return new Generic.Call<>(sig, domain().ccc(argument, vars), f.generic(argument, vars), x.generic(argument, vars));
        }

        @Override
        public <V> Category<Z, B> substitute(Type.Var<V> argument, Type<V> replacement) {
            return new Call<>(f.substitute(argument, replacement), x.substitute(argument, replacement));
        }

        @Override
        public Type<Z> domain() {
            return f.domain();
        }

        @Override
        public Type<B> range() {
            return ((Type.FunType<A, B>) f.range()).range();
        }

        public String toString() {
            return "(S " + f + " " + x + ")";
        }
    }

    record MakeLambda<Z, A extends HList, B, R>(Type<Z>domain, Type<R>range, Args<A, B, R>arguments,
                                                Category<A, B>body) implements Category<Z, R> {
        @Override
        public <X> Generic<X, F<Z, R>> generic(Type.Var<X> argument, TVarGen vars) {
            return new Generic.MakeLambda<>(domain.ccc(argument, vars), range.ccc(argument, vars),
                    body.domain().ccc(argument, vars), body.range().ccc(argument, vars),
                    body.generic(argument, vars));
        }

        @Override
        public <X> Category<Z, R> substitute(Type.Var<X> argument, Type<X> replacement) {
            throw null;
        }

        public String toString() {
            return "(λ " + body + ")";
        }
    }
}

package com.sstewartgallus;

import com.sstewartgallus.pass2.VarGen;
import com.sstewartgallus.type.*;

import java.lang.constant.ConstantDesc;
import java.util.function.Function;

/**
 * compile to a closed cartesian category
 * <p>
 * really for full enterprise javaness it should be called AbstractCategory but whatever....
 */
public interface Category<A, B> {

    static <A> Generic<Void, F<Void, A>> generic(Category<Void, A> category) {
        var vars = new VarGen();
        return category.generic(vars.createTypeVar(), vars);
    }

    // fixme.. use separate TypeVarGen type
    <V> Generic<V, F<A, B>> generic(Type.Var<V> argument, VarGen vars);

    <V> Category<A, B> substitute(Type.Var<V> argument, Type<V> replacement);

    static <A, B> Category<T<A, B>, A> first(Type<A> left, Type<B> right) {
        return new First<>(left, right);
    }

    static <A, B> Category<T<A, B>, B> second(Type<A> left, Type<B> right) {
        return new Second<>(left, right);
    }

    static <V, A, B> Category<V, B> call(Category<V, F<A, B>> f, Category<V, A> x) {
        // fixme...  which?
        // consider also just new Call<>(f, x);
        //    return new Eval<>(domain, range).compose(f.product(x));
        //    return new Uncurry<>(new Identity<>(domain.to(range))).compose(f.product(x));
        return uncurry(f).compose(new Identity<>(x.domain()).product(x));
    }

    // fixme... consider specializing to apply ?
    default <C> Category<A, T<B, C>> product(Category<A, C> x) {
        return new Product<>(this, x);
    }

    static <A, B, C> Category<A, F<B, C>> curry(Category<T<A, B>, C> f) {
        return new Curry<>(f);
    }

    static <A, B, C> Category<T<A, B>, C> uncurry(Category<A, F<B, C>> f) {
        return new Uncurry<>(f);
    }

    static <B, A> Category<B, A> constant(Type<B> domain, Type<A> range, ConstantDesc value) {
        return new Unit<A>(range, value).compose(new Initial<>(domain));
    }

    default <C> Category<C, B> compose(Category<C, A> g) {
        return new Compose<>(this, g);
    }

    Type<A> domain();

    Type<B> range();

    record Unit<A>(Type<A>range, ConstantDesc value) implements Category<Void, A> {
        public String toString() {
            return value.toString();
        }

        @Override
        public Type<Void> domain() {
            return Type.VOID;
        }

        public <V> Generic<V, F<Void, A>> generic(Type.Var<V> argument, VarGen vars) {
            var sig = new Type.FunType<>(Type.VOID, range).ccc(argument, vars);
            return new Generic.Unit<>(sig, value);
        }

        public <V> Category<Void, A> substitute(Type.Var<V> argument, Type<V> replacement) {
            return new Unit<>(range.substitute(argument, replacement), value);
        }

    }

    record Identity<A>(Type<A>type) implements Category<A, A> {

        public <V> Generic<V, F<A, A>> generic(Type.Var<V> argument, VarGen vars) {
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
            return "id";
        }

    }

    record Compose<A, B, C>(Category<B, C>f, Category<A, B>g) implements Category<A, C> {
        public <V> Generic<V, F<A, C>> generic(Type.Var<V> argument, VarGen vars) {
            var sig = new Type.FunType<>(domain(), range()).ccc(argument, vars);
            return new Generic.Compose<>(sig, f.generic(argument, vars), g.generic(argument, vars));
        }

        public <V> Category<A, C> substitute(Type.Var<V> argument, Type<V> replacement) {
            return new Compose<>(f.substitute(argument, replacement), g.substitute(argument, replacement));
        }

        public String toString() {
            return f + " ⚬ " + g;
        }

        @Override
        public Type<A> domain() {
            return g.domain();
        }

        @Override
        public Type<C> range() {
            return f.range();
        }
    }

    record Product<A, B, C>(Category<A, B>f, Category<A, C>g) implements Category<A, T<B, C>> {
        public String toString() {
            return f + " Δ " + g;
        }

        public <V> Generic<V, F<A, T<B, C>>> generic(Type.Var<V> argument, VarGen vars) {
            return new Generic.Product<>(domain().to(range()).ccc(argument, vars), f.generic(argument, vars), g.generic(argument, vars));
        }

        public <V> Category<A, T<B, C>> substitute(Type.Var<V> argument, Type<V> replacement) {
            return new Product<>(f.substitute(argument, replacement), g.substitute(argument, replacement));
        }

        @Override
        public Type<A> domain() {
            return f.domain();
        }

        @Override
        public Type<T<B, C>> range() {
            return f.range().and(g.range());
        }
    }

    // fixme... get type of the pair we are using..
    record First<A, B>(Type<A>first, Type<B>second) implements Category<T<A, B>, A> {
        public <V> Generic<V, F<T<A, B>, A>> generic(Type.Var<V> argument, VarGen vars) {
            return new Generic.First<>(domain().to(range()).ccc(argument, vars), first.ccc(argument, vars));
        }

        public <V> Category<T<A, B>, A> substitute(Type.Var<V> argument, Type<V> replacement) {
            return new First<>(first.substitute(argument, replacement), second.substitute(argument, replacement));
        }


        public String toString() {
            return "exl";
        }

        @Override
        public Type<T<A, B>> domain() {
            return first.and(second);
        }

        @Override
        public Type<A> range() {
            return first;
        }
    }

    record Second<A, B>(Type<A>first, Type<B>second) implements Category<T<A, B>, B> {
        public <V> Generic<V, F<T<A, B>, B>> generic(Type.Var<V> argument, VarGen vars) {
            return new Generic.Second<>(domain().to(range()).ccc(argument, vars), second.ccc(argument, vars));
        }

        public <V> Category<T<A, B>, B> substitute(Type.Var<V> argument, Type<V> replacement) {
            return new Second<>(first.substitute(argument, replacement), second.substitute(argument, replacement));
        }

        @Override
        public Type<T<A, B>> domain() {
            return first.and(second);
        }

        @Override
        public Type<B> range() {
            return second;
        }

        public String toString() {
            return "exr";
        }
    }

    record Curry<A, B, C>(Category<T<A, B>, C>f) implements Category<A, F<B, C>> {
        public <V> Generic<V, F<A, F<B, C>>> generic(Type.Var<V> argument, VarGen vars) {
            var prod = ((Type.ProductType<A, B>) (f.domain()));
            return new Generic.Curry<>(domain().to(range()).ccc(argument, vars),
                    prod.left().ccc(argument, vars),
                    prod.right().ccc(argument, vars),
                    f.generic(argument, vars));
        }

        public <V> Category<A, F<B, C>> substitute(Type.Var<V> argument, Type<V> replacement) {
            return new Curry<>(f.substitute(argument, replacement));
        }

        @Override
        public Type<A> domain() {
            return ((Type.ProductType<A, B>) (f.domain())).left();
        }

        @Override
        public Type<F<B, C>> range() {
            return second().to(f.range());
        }

        public Type<B> second() {
            return ((Type.ProductType<A, B>) (f.domain())).right();
        }

        public String toString() {
            return "(curry " + f + ")";
        }
    }

    // fixme... needed for intrinsics...
    // basically eval, could you define it in terms of eval?
    record Uncurry<A, B, C>(Category<A, F<B, C>>f) implements Category<T<A, B>, C> {
        public <V> Generic<V, F<T<A, B>, C>> generic(Type.Var<V> argument, VarGen vars) {
            return new Generic.Uncurry<>(f.generic(argument, vars));
        }

        public <V> Category<T<A, B>, C> substitute(Type.Var<V> argument, Type<V> replacement) {
            return new Uncurry<>(f.substitute(argument, replacement));
        }

        @Override
        public Type<T<A, B>> domain() {
            return f.domain().and(second());
        }

        @Override
        public Type<C> range() {
            return ((Type.FunType<B, C>) (f.range())).range();
        }

        public Type<B> second() {
            return ((Type.FunType<B, C>) (f.range())).domain();
        }

        public String toString() {
            return "(uncurry " + f + ")";
        }
    }

    record Initial<A>(Type<A>domain) implements Category<A, Void> {
        public <V> Generic<V, F<A, Void>> generic(Type.Var<V> argument, VarGen vars) {
            var sig = domain.ccc(argument, vars);
            return new Generic.Initial<>(sig);
        }

        public <V> Category<A, Void> substitute(Type.Var<V> argument, Type<V> replacement) {
            return new Initial<>(domain.substitute(argument, replacement));
        }

        public String toString() {
            return "it";
        }

        @Override
        public Type<Void> range() {
            return Type.VOID;
        }
    }

    record Exists<X, A, B>(Type<A>x, Category<X, B>y) implements Category<X, E<A, B>> {

        @Override
        public <Z> Generic<Z, F<X, E<A, B>>> generic(Type.Var<Z> argument, VarGen vars) {
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
        public <Z> Generic<Z, F<X, V<A, B>>> generic(Type.Var<Z> argument, VarGen vars) {
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
}

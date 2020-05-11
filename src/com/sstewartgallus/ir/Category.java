package com.sstewartgallus.ir;

import com.sstewartgallus.pass1.Args;
import com.sstewartgallus.type.*;

import java.lang.constant.ConstantDesc;
import java.util.function.Function;

/**
 * compile to a closed cartesian category
 * <p>
 * really for full enterprise javaness it should be called AbstractCategory but whatever....
 * <p>
 * Fixme... not really a category anymore is it?
 */
public interface Category<A extends HList<A>, B> extends PointFree<F<A, B>> {

    static <A> Generic<Void, F<HList.Nil, A>> generic(Category<HList.Nil, A> category) {
        var vars = new TVarGen();
        return category.generic(vars.createTypeVar(), vars);
    }

    static <V extends HList<V>, A, B> Category<V, B> call(Category<V, F<A, B>> f, Category<V, A> x) {
        return new Call<>(f, x);
    }

    static <B extends HList<B>, A> Category<B, A> constant(Type<B> domain, Type<A> range, ConstantDesc value) {
        return new Unit<>(domain, range, value);
    }

    static <A extends HList<A>, R, B, Z extends HList<Z>> Category<Z, R> makeLambda(Type<Z> domain, Type<R> range, Args<A, B, R> arguments, Category<A, B> ccc) {
        return new Lambda<>(domain, range, arguments, ccc);
    }

    // fixme.. use separate TypeVarGen type
    <Z> Generic<Z, F<A, B>> generic(Type.Var<Z> argument, TVarGen vars);

    <Z> Category<A, B> substitute(Type.Var<Z> argument, Type<Z> replacement);

    Type<A> domain();

    Type<B> range();

    record Unit<A extends HList<A>, B>(Type<A>domain, Type<B>range, ConstantDesc value) implements Category<A, B> {
        public String toString() {
            return "(K " + value.toString() + ")";
        }

        public <V> Generic<V, F<A, B>> generic(Type.Var<V> argument, TVarGen vars) {
            var sig = domain.to(range).ccc(argument, vars);
            return new Generic.Unit<>(sig, domain.ccc(argument, vars), range.ccc(argument, vars), value);
        }

        public <V> Category<A, B> substitute(Type.Var<V> argument, Type<V> replacement) {
            return new Unit<>(domain.substitute(argument, replacement), range.substitute(argument, replacement), value);
        }
    }

    record Get<A extends HList<A>, B extends HList<B>, X>(Type<A>type,
                                                          com.sstewartgallus.pass1.Index<A, HList.Cons<X, B>>ix) implements Category<A, X> {

        public <V> Generic<V, F<A, X>> generic(Type.Var<V> argument, TVarGen vars) {
            var sig = new Type.FunType<>(domain(), range()).ccc(argument, vars);
            return new Generic.Get<>(sig, type.ccc(argument, vars), ix);
        }

        public <V> Category<A, X> substitute(Type.Var<V> argument, Type<V> replacement) {
            return new Get<>(type.substitute(argument, replacement), ix.substitute(argument, replacement));
        }

        @Override
        public Type<A> domain() {
            return type;
        }

        @Override
        public Type<X> range() {
            return ((Type.ConsType<X, B>) ix.range()).head();
        }

        public String toString() {
            return "[" + ix + "]";
        }
    }

    record Exists<X extends HList<X>, A, B>(Type<A>x, Category<X, B>y) implements Category<X, E<A, B>> {

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

    record Forall<X extends HList<X>, A, B>(Type<X>domain,
                                            Function<Type<A>, Category<X, B>>f) implements Category<X, V<A, B>> {
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

    record Call<Z extends HList<Z>, A, B>(Category<Z, F<A, B>>f, Category<Z, A>x) implements Category<Z, B> {
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

    record Lambda<Z extends HList<Z>, A extends HList<A>, B, R>(Type<Z>domain, Type<R>range, Args<A, B, R>arguments,
                                                                Category<A, B>body) implements Category<Z, R> {
        @Override
        public <X> Generic<X, F<Z, R>> generic(Type.Var<X> argument, TVarGen vars) {
            return new Generic.Lambda<>(
                    domain.to(range).ccc(argument, vars),
                    domain.ccc(argument, vars), range.ccc(argument, vars),
                    body.domain().ccc(argument, vars), body.range().ccc(argument, vars),
                    body.generic(argument, vars));
        }

        @Override
        public <X> Category<Z, R> substitute(Type.Var<X> argument, Type<X> replacement) {
            throw null;
        }

        public String toString() {
            return "(λ" + arguments + " " + body + ")";
        }
    }
}

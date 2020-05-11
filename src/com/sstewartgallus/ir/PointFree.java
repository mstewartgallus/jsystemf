package com.sstewartgallus.ir;

import com.sstewartgallus.pass1.Args;
import com.sstewartgallus.type.*;

import java.lang.constant.ConstantDesc;
import java.util.function.Function;

public interface PointFree<A> {
    static <A> Generic<Void, F<HList.Nil, A>> generic(PointFree<F<HList.Nil, A>> category) {
        var vars = new TVarGen();
        return category.generic(vars.createTypeVar(), vars);
    }

    static <V extends HList<V>, A, B> PointFree<F<V, B>> call(PointFree<F<V, F<A, B>>> f, PointFree<F<V, A>> x) {
        return new Call<>(f, x);
    }

    static <B extends HList<B>, A> PointFree<F<B, A>> constant(Type<B> domain, Type<A> range, ConstantDesc value) {
        return new K<>(domain, new Con<>(range, value));
    }

    static <A extends HList<A>, R, B, Z extends HList<Z>> PointFree<F<Z, R>> lambda(Type<Z> domain, Type<R> range, Args<A, B, R> arguments, PointFree<F<A, B>> ccc) {
        return new K<>(domain, new Lambda<>(range, arguments, ccc));
    }

    <Z> Generic<Z, A> generic(Type.Var<Z> argument, TVarGen vars);

    <Z> PointFree<A> substitute(Type.Var<Z> argument, Type<Z> replacement);

    Type<A> type();

    record Con<B>(Type<B>type, ConstantDesc value) implements PointFree<B> {
        public String toString() {
            return value.toString();
        }

        public <V> Generic<V, B> generic(Type.Var<V> argument, TVarGen vars) {
            var sig = type().pointFree(argument, vars);
            return new Generic.Con<>(sig, value);
        }

        public <V> PointFree<B> substitute(Type.Var<V> argument, Type<V> replacement) {
            return new Con<>(type.substitute(argument, replacement), value);
        }
    }

    record K<A extends HList<A>, B>(Type<A>domain, PointFree<B>value) implements PointFree<F<A, B>> {
        public String toString() {
            return "(K " + value.toString() + ")";
        }

        public <V> Generic<V, F<A, B>> generic(Type.Var<V> argument, TVarGen vars) {
            var sig = type().pointFree(argument, vars);
            return new Generic.K<>(sig, domain.pointFree(argument, vars), value.generic(argument, vars));
        }

        public <V> PointFree<F<A, B>> substitute(Type.Var<V> argument, Type<V> replacement) {
            return new K<>(domain.substitute(argument, replacement), value);
        }

        @Override
        public Type<F<A, B>> type() {
            return domain.to(value.type());
        }
    }

    record Get<A extends HList<A>, B extends HList<B>, X>(Type<A>domain,
                                                          com.sstewartgallus.pass1.Index<A, HList.Cons<X, B>>ix) implements PointFree<F<A, X>> {

        public <V> Generic<V, F<A, X>> generic(Type.Var<V> argument, TVarGen vars) {
            var sig = type().pointFree(argument, vars);
            return new Generic.Get<>(sig, domain.pointFree(argument, vars), ix);
        }

        public <V> PointFree<F<A, X>> substitute(Type.Var<V> argument, Type<V> replacement) {
            return new Get<>(domain.substitute(argument, replacement), ix.substitute(argument, replacement));
        }

        public Type<F<A, X>> type() {
            return domain.to(((Type.ConsType<X, B>) ix.range()).head());
        }

        public String toString() {
            return "[" + ix + "]";
        }
    }

    record Exists<X extends HList<X>, A, B>(Type<A>x, PointFree<F<X, B>>y) implements PointFree<F<X, E<A, B>>> {

        @Override
        public <Z> Generic<Z, F<X, E<A, B>>> generic(Type.Var<Z> argument, TVarGen vars) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public <Z> PointFree<F<X, E<A, B>>> substitute(Type.Var<Z> argument, Type<Z> replacement) {
            throw new UnsupportedOperationException("unimplemented");
        }

        public Type<F<X, E<A, B>>> type() {
            throw null;
        }
    }

    record Forall<X extends HList<X>, A, B>(Type<X>domain,
                                            Function<Type<A>, PointFree<F<X, B>>>f) implements PointFree<F<X, V<A, B>>> {
        public <Z> Generic<Z, F<X, V<A, B>>> generic(Type.Var<Z> argument, TVarGen vars) {
            Type.Var<E<Z, A>> arg = vars.createTypeVar();

            var signature = type().pointFree(argument, vars);

            Generic<E<Z, A>, F<X, B>> body = f.apply(new Type.Second<>(arg))
                    .substitute(argument, new Type.First<>(arg))
                    .generic(arg, vars);
            return Generic.curry(signature, body);
        }

        public <Z> PointFree<F<X, V<A, B>>> substitute(Type.Var<Z> argument, Type<Z> replacement) {
            return new Forall<>(domain, arg -> f.apply(arg).substitute(argument, replacement));
        }

        public Type<F<X, V<A, B>>> type() {
            throw null;
        }
    }

    record Call<Z extends HList<Z>, A, B>(PointFree<F<Z, F<A, B>>>f,
                                          PointFree<F<Z, A>>x) implements PointFree<F<Z, B>> {
        @Override
        public <V> Generic<V, F<Z, B>> generic(Type.Var<V> argument, TVarGen vars) {
            var sig = type().pointFree(argument, vars);
            var domain = ((Type.FunType<Z, F<A, B>>) f.type()).domain();
            return new Generic.Call<>(sig, domain.pointFree(argument, vars), f.generic(argument, vars), x.generic(argument, vars));
        }

        @Override
        public <V> PointFree<F<Z, B>> substitute(Type.Var<V> argument, Type<V> replacement) {
            return new Call<>(f.substitute(argument, replacement), x.substitute(argument, replacement));
        }

        @Override
        public Type<F<Z, B>> type() {
            var domain = ((Type.FunType<Z, F<A, B>>) f.type()).domain();
            var range = ((Type.FunType<A, B>) ((Type.FunType<Z, F<A, B>>) f.type()).range()).range();
            return domain.to(range);
        }

        public String toString() {
            return "(S " + f + " " + x + ")";
        }
    }

    record Lambda<A extends HList<A>, B, R>(Type<R>type, Args<A, B, R>arguments,
                                            PointFree<F<A, B>>body) implements PointFree<R> {
        @Override
        public <X> Generic<X, R> generic(Type.Var<X> argument, TVarGen vars) {
            var bodyT = (Type.FunType<A, B>) body.type();
            return new Generic.Lambda<>(
                    type().pointFree(argument, vars),
                    bodyT.domain().pointFree(argument, vars), bodyT.range().pointFree(argument, vars),
                    body.generic(argument, vars));
        }

        @Override
        public <X> PointFree<R> substitute(Type.Var<X> argument, Type<X> replacement) {
            throw null;
        }

        public String toString() {
            return "(λ" + arguments + " " + body + ")";
        }
    }
}

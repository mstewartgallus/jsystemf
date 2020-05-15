package com.sstewartgallus.ir;

import com.sstewartgallus.ext.java.JavaType;
import com.sstewartgallus.ext.tuples.*;
import com.sstewartgallus.ext.variables.VarType;
import com.sstewartgallus.plato.F;
import com.sstewartgallus.plato.FunctionType;
import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.V;

public interface PointFree<A> {
    <Z> Generic<V<Z, A>> generic(VarType<Z> argument);

    <Z> PointFree<A> substitute(VarType<Z> argument, Type<Z> replacement);

    Type<A> type();

    record IntValue(int value) implements PointFree<Integer> {
        public String toString() {
            return String.valueOf(value);
        }

        public <X> Generic<V<X, Integer>> generic(VarType<X> argument) {
            return new Generic.IntValue<>(type().pointFree(argument), value);
        }

        public <X> PointFree<Integer> substitute(VarType<X> argument, Type<X> replacement) {
            return this;
        }

        @Override
        public Type<Integer> type() {
            return new JavaType<>(int.class);
        }
    }

    record K<A extends HList<A>, B>(Type<A>domain, PointFree<B>value) implements PointFree<F<A, B>> {
        public String toString() {
            return "(K " + value.toString() + ")";
        }

        public <V> Generic<com.sstewartgallus.plato.V<V, F<A, B>>> generic(VarType<V> argument) {
            var sig = type().pointFree(argument);
            return new GenericV.K<>(sig, domain.pointFree(argument), value.generic(argument));
        }

        public <V> PointFree<F<A, B>> substitute(VarType<V> argument, Type<V> replacement) {
            return new K<>(argument.substituteIn(domain, replacement), value);
        }

        @Override
        public Type<F<A, B>> type() {
            return new FunctionType<>(domain, value.type());
        }
    }

    record Get<A extends HList<A>, B extends HList<B>, X>(Type<A>domain,
                                                          Index<A, Cons<X, B>>ix) implements PointFree<F<A, X>> {

        public <V> Generic<com.sstewartgallus.plato.V<V, F<A, X>>> generic(VarType<V> argument) {
            var sig = type().pointFree(argument);
            return new GenericV.Get<>(sig, domain.pointFree(argument), ix);
        }

        public <V> PointFree<F<A, X>> substitute(VarType<V> argument, Type<V> replacement) {
            return new Get<>(argument.substituteIn(domain, replacement), ix.substitute(argument, replacement));
        }

        public Type<F<A, X>> type() {
            return new FunctionType<>(domain, ((ConsType<X, B>) ix.range()).head());
        }

        public String toString() {
            return "[" + ix + "]";
        }
    }

    record Call<Z extends HList<Z>, A, B>(PointFree<F<Z, F<A, B>>>f,
                                          PointFree<F<Z, A>>x) implements PointFree<F<Z, B>> {
        @Override
        public <V> Generic<com.sstewartgallus.plato.V<V, F<Z, B>>> generic(VarType<V> argument) {
            var sig = type().pointFree(argument);
            var domain = ((FunctionType<Z, F<A, B>>) f.type()).domain();
            return new GenericV.Call<>(sig, domain.pointFree(argument), f.generic(argument), x.generic(argument));
        }

        @Override
        public <V> PointFree<F<Z, B>> substitute(VarType<V> argument, Type<V> replacement) {
            return new Call<>(f.substitute(argument, replacement), x.substitute(argument, replacement));
        }

        @Override
        public Type<F<Z, B>> type() {
            var domain = ((FunctionType<Z, F<A, B>>) f.type()).domain();
            var range = ((FunctionType<A, B>) ((FunctionType<Z, F<A, B>>) f.type()).range()).range();
            return new FunctionType<>(domain, range);
        }

        public String toString() {
            return "(S " + f + " " + x + ")";
        }
    }

    record Lambda<A extends HList<A>, B, R>(UncurryLambdaThunk.Sig<A, B, R>sig,
                                            PointFree<F<A, B>>body) implements PointFree<R> {
        @Override
        public <X> Generic<V<X, R>> generic(VarType<X> argument) {
            var bodyT = (FunctionType<A, B>) body.type();
            return new GenericV.Lambda<>(
                    type().pointFree(argument),
                    bodyT.domain().pointFree(argument), bodyT.range().pointFree(argument),
                    body.generic(argument));
        }

        @Override
        public <X> PointFree<R> substitute(VarType<X> argument, Type<X> replacement) {
            throw null;
        }

        @Override
        public Type<R> type() {
            return sig.type();
        }

        public String toString() {
            return "(λ" + sig + " " + body + ")";
        }
    }

    record Pick<T, A>(PointFree<F<T, F<Nil, A>>>k) implements PointFree<F<T, A>> {
        @Override
        public <Z> Generic<V<Z, F<T, A>>> generic(VarType<Z> argument) {
            throw null;
        }

        @Override
        public <Z> PointFree<F<T, A>> substitute(VarType<Z> argument, Type<Z> replacement) {
            throw null;
        }

        @Override
        public Type<F<T, A>> type() {
            var kType = (FunctionType<T, F<Nil, A>>) k.type();
            var range = ((FunctionType<Nil, A>) kType.range());
            return kType.domain().to(range.range());
        }

        public String toString() {
            return "!" + k;
        }
    }

    record KThunk<T, B, C, A>(PointFree<F<T, F<B, F<C, A>>>>f,
                              PointFree<F<T, F<B, C>>>x) implements PointFree<F<T, F<B, A>>> {
        @Override
        public <Z> Generic<V<Z, F<T, F<B, A>>>> generic(VarType<Z> argument) {
            return null;
        }

        @Override
        public <Z> PointFree<F<T, F<B, A>>> substitute(VarType<Z> argument, Type<Z> replacement) {
            return null;
        }

        @Override
        public Type<F<T, F<B, A>>> type() {
            return null;
        }
    }
}

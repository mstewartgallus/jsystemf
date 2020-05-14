package com.sstewartgallus.ir;

import com.sstewartgallus.extensions.tuples.ConsNormal;
import com.sstewartgallus.extensions.tuples.HList;
import com.sstewartgallus.extensions.tuples.Index;
import com.sstewartgallus.extensions.tuples.UncurryLambdaThunk;
import com.sstewartgallus.plato.*;

public interface PointFree<A> {
    <Z> Generic<V<Z, A>> generic(Id<Z> argument, IdGen vars);

    <Z> PointFree<A> substitute(Id<Z> argument, Type<Z> replacement);

    Type<A> type();

    record K<A extends HList<A>, B>(Type<A>domain, PointFree<B>value) implements PointFree<F<A, B>> {
        public String toString() {
            return "(K " + value.toString() + ")";
        }

        public <V> Generic<com.sstewartgallus.plato.V<V, F<A, B>>> generic(Id<V> argument, IdGen vars) {
            var sig = type().pointFree(argument, vars);
            return new GenericV.K<>(sig, domain.pointFree(argument, vars), value.generic(argument, vars));
        }

        public <V> PointFree<F<A, B>> substitute(Id<V> argument, Type<V> replacement) {
            return new K<>(domain.substitute(argument, replacement), value);
        }

        @Override
        public Type<F<A, B>> type() {
            return new FunctionNormal<>(domain, value.type());
        }
    }

    record Get<A extends HList<A>, B extends HList<B>, X>(Type<A>domain,
                                                          Index<A, HList.Cons<X, B>>ix) implements PointFree<F<A, X>> {

        public <V> Generic<com.sstewartgallus.plato.V<V, F<A, X>>> generic(Id<V> argument, IdGen vars) {
            var sig = type().pointFree(argument, vars);
            return new GenericV.Get<>(sig, domain.pointFree(argument, vars), ix);
        }

        public <V> PointFree<F<A, X>> substitute(Id<V> argument, Type<V> replacement) {
            return new Get<>(domain.substitute(argument, replacement), ix.substitute(argument, replacement));
        }

        public Type<F<A, X>> type() {
            return new FunctionNormal<>(domain, ((ConsNormal<X, B>) ix.range()).head());
        }

        public String toString() {
            return "[" + ix + "]";
        }
    }

    record Call<Z extends HList<Z>, A, B>(PointFree<F<Z, F<A, B>>>f,
                                          PointFree<F<Z, A>>x) implements PointFree<F<Z, B>> {
        @Override
        public <V> Generic<com.sstewartgallus.plato.V<V, F<Z, B>>> generic(Id<V> argument, IdGen vars) {
            var sig = type().pointFree(argument, vars);
            var domain = ((FunctionNormal<Z, F<A, B>>) f.type()).domain();
            return new GenericV.Call<>(sig, domain.pointFree(argument, vars), f.generic(argument, vars), x.generic(argument, vars));
        }

        @Override
        public <V> PointFree<F<Z, B>> substitute(Id<V> argument, Type<V> replacement) {
            return new Call<>(f.substitute(argument, replacement), x.substitute(argument, replacement));
        }

        @Override
        public Type<F<Z, B>> type() {
            var domain = ((FunctionNormal<Z, F<A, B>>) f.type()).domain();
            var range = ((FunctionNormal<A, B>) ((FunctionNormal<Z, F<A, B>>) f.type()).range()).range();
            return new FunctionNormal<>(domain, range);
        }

        public String toString() {
            return "(S " + f + " " + x + ")";
        }
    }

    record Lambda<A extends HList<A>, B, R>(UncurryLambdaThunk.Sig<A, B, R>sig,
                                            PointFree<F<A, B>>body) implements PointFree<R> {
        @Override
        public <X> Generic<V<X, R>> generic(Id<X> argument, IdGen vars) {
            var bodyT = (FunctionNormal<A, B>) body.type();
            return new GenericV.Lambda<>(
                    type().pointFree(argument, vars),
                    bodyT.domain().pointFree(argument, vars), bodyT.range().pointFree(argument, vars),
                    body.generic(argument, vars));
        }

        @Override
        public <X> PointFree<R> substitute(Id<X> argument, Type<X> replacement) {
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
}

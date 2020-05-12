package com.sstewartgallus.ir;

import com.sstewartgallus.pass1.Args;
import com.sstewartgallus.pass1.HList;
import com.sstewartgallus.pass1.TPass0;
import com.sstewartgallus.plato.F;
import com.sstewartgallus.plato.Id;
import com.sstewartgallus.plato.IdGen;
import com.sstewartgallus.plato.V;

public interface PointFree<A> {
    <Z> Generic<V<Z, A>> generic(Id<Z> argument, IdGen vars);

    <Z> PointFree<A> substitute(Id<Z> argument, TPass0<Z> replacement);

    TPass0<A> type();

    record K<A extends HList<A>, B>(TPass0<A>domain, PointFree<B>value) implements PointFree<F<A, B>> {
        public String toString() {
            return "(K " + value.toString() + ")";
        }

        public <V> Generic<com.sstewartgallus.plato.V<V, F<A, B>>> generic(Id<V> argument, IdGen vars) {
            var sig = type().pointFree(argument, vars);
            return new GenericV.K<>(sig, domain.pointFree(argument, vars), value.generic(argument, vars));
        }

        public <V> PointFree<F<A, B>> substitute(Id<V> argument, TPass0<V> replacement) {
            return new K<>(domain.substitute(argument, replacement), value);
        }

        @Override
        public TPass0<F<A, B>> type() {
            return new TPass0.FunType<>(domain, value.type());
        }
    }

    record Get<A extends HList<A>, B extends HList<B>, X>(TPass0<A>domain,
                                                          com.sstewartgallus.pass1.Index<A, HList.Cons<X, B>>ix) implements PointFree<F<A, X>> {

        public <V> Generic<com.sstewartgallus.plato.V<V, F<A, X>>> generic(Id<V> argument, IdGen vars) {
            var sig = type().pointFree(argument, vars);
            return new GenericV.Get<>(sig, domain.pointFree(argument, vars), ix);
        }

        public <V> PointFree<F<A, X>> substitute(Id<V> argument, TPass0<V> replacement) {
            return new Get<>(domain.substitute(argument, replacement), ix.substitute(argument, replacement));
        }

        public TPass0<F<A, X>> type() {
            return new TPass0.FunType<>(domain, ((TPass0.ConsType<X, B>) ix.range()).head());
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
            var domain = ((TPass0.FunType<Z, F<A, B>>) f.type()).domain();
            return new GenericV.Call<>(sig, domain.pointFree(argument, vars), f.generic(argument, vars), x.generic(argument, vars));
        }

        @Override
        public <V> PointFree<F<Z, B>> substitute(Id<V> argument, TPass0<V> replacement) {
            return new Call<>(f.substitute(argument, replacement), x.substitute(argument, replacement));
        }

        @Override
        public TPass0<F<Z, B>> type() {
            var domain = ((TPass0.FunType<Z, F<A, B>>) f.type()).domain();
            var range = ((TPass0.FunType<A, B>) ((TPass0.FunType<Z, F<A, B>>) f.type()).range()).range();
            return new TPass0.FunType<>(domain, range);
        }

        public String toString() {
            return "(S " + f + " " + x + ")";
        }
    }

    record Lambda<A extends HList<A>, B, R>(TPass0<R>type, Args<A, B, R>arguments,
                                            PointFree<F<A, B>>body) implements PointFree<R> {
        @Override
        public <X> Generic<V<X, R>> generic(Id<X> argument, IdGen vars) {
            var bodyT = (TPass0.FunType<A, B>) body.type();
            return new GenericV.Lambda<>(
                    type().pointFree(argument, vars),
                    bodyT.domain().pointFree(argument, vars), bodyT.range().pointFree(argument, vars),
                    body.generic(argument, vars));
        }

        @Override
        public <X> PointFree<R> substitute(Id<X> argument, TPass0<X> replacement) {
            throw null;
        }

        public String toString() {
            return "(λ" + arguments + " " + body + ")";
        }
    }
}

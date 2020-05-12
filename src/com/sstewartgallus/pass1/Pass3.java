package com.sstewartgallus.pass1;

import com.sstewartgallus.ir.PointFree;
import com.sstewartgallus.plato.F;
import com.sstewartgallus.plato.Id;
import com.sstewartgallus.plato.IdGen;

import java.util.Objects;
import java.util.function.Function;

public interface Pass3<A> {
    TPass0<A> type();

    <V> Pass3<A> substitute(Id<V> argument, Pass3<V> replacement);

    <T extends HList<T>> PointFree<F<T, A>> pointFree(Id<T> argument, IdGen vars, TPass0<T> argType);

    record Apply<A, B>(Pass3<F<A, B>>f, Pass3<A>x) implements Pass3<B> {
        public <V> Pass3<B> substitute(Id<V> argument, Pass3<V> replacement) {
            return new Apply<>(f.substitute(argument, replacement), x.substitute(argument, replacement));
        }

        @Override
        public <T extends HList<T>> PointFree<F<T, B>> pointFree(Id<T> argument, IdGen vars, TPass0<T> argType) {
            var fCcc = f.pointFree(argument, vars, argType);
            var xCcc = x.pointFree(argument, vars, argType);
            return new PointFree.Call<>(fCcc, xCcc);
        }

        public TPass0<B> type() {
            var funTPass0 = ((TPass0.FunType<A, B>) f.type());
            var t = x.type();
            if (!Objects.equals(t, funTPass0.domain())) {
                throw new RuntimeException("type error");
            }
            return funTPass0.range();
        }

        public String toString() {
            return "(" + f + " " + x + ")";
        }
    }

    // fixme... eventually eliminate
    record WrapGet<C extends HList<C>, A, B extends HList<B>>(Get<C, HList.Cons<A, B>>list) implements Pass3<A> {
        public String toString() {
            return list.toString();
        }

        public <V> Pass3<A> substitute(Id<V> argument, Pass3<V> replacement) {
            if (list.variable == argument) {
                return (Pass3) replacement;
            }
            return this;
        }

        @Override
        public <T extends HList<T>> PointFree<F<T, A>> pointFree(Id<T> argument, IdGen vars, TPass0<T> argType) {
            if (argument == list.variable) {
                return (PointFree) new PointFree.Get<>(list.type(), list.ix);
            }
            throw new IllegalStateException("mismatching variables " + list);
        }

        @Override
        public TPass0<A> type() {
            return ((TPass0.ConsType<A, B>) list.type()).head();
        }
    }

    record Get<A extends HList<A>, B extends HList<B>>(TPass0<A>type, Id<A>variable, Index<A, B>ix) {
        @Override
        public String toString() {
            return variable + "[" + ix + "]";
        }
    }

    record Lambda<A extends HList<A>, B, R>(TPass0<R>type, TPass0<A>domain, Args<A, B, R>arguments,
                                            Function<Id<A>, Pass3<B>>f) implements Pass3<R> {
        private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

        public <V> Pass3<R> substitute(Id<V> argument, Pass3<V> replacement) {
            return new Lambda<>(type, domain, arguments, x -> f.apply(x).substitute(argument, replacement));
        }

        @Override
        public <T extends HList<T>> PointFree<F<T, R>> pointFree(Id<T> argument, IdGen vars, TPass0<T> argType) {
            var arg = vars.<A>createId();
            var body = f.apply(arg);
            var ccc = body.pointFree(arg, vars, domain);

            return new PointFree.K<>(argType, new PointFree.Lambda<>(this.type, this.arguments, ccc));
        }

        public String toString() {
            var depth = DEPTH.get();
            DEPTH.set(depth + 1);

            String str;
            try {
                var dummy = new Id<A>(depth);
                var body = f.apply(dummy);

                str = "{" + dummy + ": " + domain + "} → " + body;
            } finally {
                DEPTH.set(depth);
                if (depth == 0) {
                    DEPTH.remove();
                }
            }
            return str;
        }
    }
}

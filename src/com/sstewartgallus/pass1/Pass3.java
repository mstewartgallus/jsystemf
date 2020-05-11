package com.sstewartgallus.pass1;

import com.sstewartgallus.ir.PointFree;
import com.sstewartgallus.ir.VarGen;
import com.sstewartgallus.type.F;
import com.sstewartgallus.type.HList;
import com.sstewartgallus.type.Var;

import java.lang.constant.ConstantDesc;
import java.util.Objects;
import java.util.function.Function;

public interface Pass3<A> {
    TPass0<A> type();

    default <V> Pass3<A> substitute(Var<V> argument, Pass3<V> replacement) {
        throw null;
    }

    <T extends HList<T>> PointFree<F<T, A>> pointFree(Var<T> argument, VarGen vars, TPass0<T> argType);

    record Apply<A, B>(Pass3<F<A, B>>f, Pass3<A>x) implements Pass3<B> {
        public <V> Pass3<B> substitute(Var<V> argument, Pass3<V> replacement) {
            return new Apply<>(f.substitute(argument, replacement), x.substitute(argument, replacement));
        }

        @Override
        public <T extends HList<T>> PointFree<F<T, B>> pointFree(Var<T> argument, VarGen vars, TPass0<T> argType) {
            var fCcc = f.pointFree(argument, vars, argType);
            var xCcc = x.pointFree(argument, vars, argType);
            return PointFree.call(fCcc, xCcc);
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
            return "{" + f + " " + x + "}";
        }
    }

    // fixme... eventually eliminated
    record WrapGet<C extends HList<C>, A, B extends HList<B>>(Get<C, HList.Cons<A, B>>list) implements Pass3<A> {
        public String toString() {
            return list.toString();
        }

        public <V> Pass3<A> substitute(Var<V> argument, Pass3<V> replacement) {
            throw null;
        }

        @Override
        public <T extends HList<T>> PointFree<F<T, A>> pointFree(Var<T> argument, VarGen vars, TPass0<T> argType) {
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

    record Load<A>(TPass0<A>type, Var<A>variable) implements Pass3<A> {
        public <V extends HList<V>> PointFree<F<V, A>> pointFree(Var<V> argument, VarGen vars, TPass0<V> argType) {
            throw new UnsupportedOperationException("unimplemented");
        }

        public <V> Pass3<A> substitute(Var<V> argument, Pass3<V> replacement) {
            if (argument == variable) {
                return (Pass3<A>) replacement;
            }
            return this;
        }

        public String toString() {
            return variable.toString();
        }
    }

    record Get<A extends HList<A>, B extends HList<B>>(TPass0<A>type, Var<A>variable, Index<A, B>ix) {
        @Override
        public String toString() {
            return variable + "[" + ix + "]";
        }
    }

    record Lambda<A extends HList<A>, B, R>(TPass0<A>domain, TPass0<R>range, Args<A, B, R>arguments,
                                            Function<Var<A>, Pass3<B>>f) implements Pass3<R> {
        private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

        public <V> Pass3<R> substitute(Var<V> argument, Pass3<V> replacement) {
            return new Lambda<>(domain, range, arguments, x -> f.apply(x).substitute(argument, replacement));
        }

        public TPass0<R> type() {
            return range;
        }

        @Override
        public <T extends HList<T>> PointFree<F<T, R>> pointFree(Var<T> argument, VarGen vars, TPass0<T> argType) {
            var arg = vars.<A>createArgument();
            var body = f.apply(arg);
            var ccc = body.pointFree(arg, vars, domain);

            return PointFree.lambda(argType, range, arguments, ccc);
        }

        public String toString() {
            var depth = DEPTH.get();
            DEPTH.set(depth + 1);

            String str;
            try {
                var dummy = new Var<A>(depth);
                var body = f.apply(dummy);

                str = "{" + dummy + ": " + domain + "} -> " + body;
            } finally {
                DEPTH.set(depth);
                if (depth == 0) {
                    DEPTH.remove();
                }
            }
            return str;
        }
    }

    record Pure<A>(TPass0<A>type, ConstantDesc value) implements Pass3<A> {
        @Override
        public <V> Pass3<A> substitute(Var<V> argument, Pass3<V> replacement) {
            return this;
        }

        @Override
        public <T extends HList<T>> PointFree<F<T, A>> pointFree(Var<T> argument, VarGen vars, TPass0<T> argType) {
            return PointFree.constant(argType, type, value);
        }

        public TPass0<A> type() {
            return type;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
}

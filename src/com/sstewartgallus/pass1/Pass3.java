package com.sstewartgallus.pass1;

import com.sstewartgallus.ir.Category;
import com.sstewartgallus.ir.VarGen;
import com.sstewartgallus.term.Var;
import com.sstewartgallus.type.F;
import com.sstewartgallus.type.HList;
import com.sstewartgallus.type.Type;

import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.util.Objects;
import java.util.function.Function;

public interface Pass3<A> {
    Type<A> type();

    default <V> Pass3<A> substitute(Var<V> argument, Pass3<V> replacement) {
        throw null;
    }

    <T extends HList<T>> Category<T, A> ccc(Var<T> argument, VarGen vars);

    record Apply<A, B>(Pass3<F<A, B>>f, Pass3<A>x) implements Pass3<B> {
        public <V> Pass3<B> substitute(Var<V> argument, Pass3<V> replacement) {
            return new Apply<>(f.substitute(argument, replacement), x.substitute(argument, replacement));
        }

        @Override
        public <T extends HList<T>> Category<T, B> ccc(Var<T> argument, VarGen vars) {
            var fCcc = f.ccc(argument, vars);
            var xCcc = x.ccc(argument, vars);
            return Category.call(fCcc, xCcc);
        }

        public Type<B> type() {
            var funType = ((Type.FunType<A, B>) f.type());
            var t = x.type();
            if (!Objects.equals(t, funType.domain())) {
                throw new RuntimeException("type error");
            }
            return funType.range();
        }

        public String toString() {
            return "{" + f + " " + x + "}";
        }
    }

    interface Index<A> {
        <V> Index<A> substitute(Var<V> argument, Pass3<V> replacement);

        // fixme... remove....
        <T extends HList<T>> Category<T, A> ccc(Var<T> argument, VarGen vars);

        // fixme... remove..
        Type<A> type();
    }

    // fixme... use an hlist of args instead...
    record Head<A, B extends HList<B>>(Index<HList.Cons<A, B>>list) implements Pass3<A> {
        public String toString() {
            return "(head " + list + ")";
        }

        public <V> Pass3<A> substitute(Var<V> argument, Pass3<V> replacement) {
            return new Head<>(list.substitute(argument, replacement));
        }

        @Override
        public <T extends HList<T>> Category<T, A> ccc(Var<T> argument, VarGen vars) {
            var prod = list.ccc(argument, vars);
            return Category.head(prod);
        }

        @Override
        public Type<A> type() {
            return ((Type.ConsType<A, B>) list.type()).head();
        }
    }

    record Tail<A, B extends HList<B>>(Index<HList.Cons<A, B>>list) implements Index<B> {
        public String toString() {
            return "(tail " + list + ")";
        }

        public <V> Index<B> substitute(Var<V> argument, Pass3<V> replacement) {
            return new Tail<>(list.substitute(argument, replacement));
        }

        @Override
        public <T extends HList<T>> Category<T, B> ccc(Var<T> argument, VarGen vars) {
            var prod = list.ccc(argument, vars);
            return Category.tail(prod);
        }

        @Override
        public Type<B> type() {
            return ((Type.ConsType<A, B>) list.type()).tail();
        }
    }

    record Load<A>(Var<A>variable) implements Pass3<A> {

        @Override
        public Type<A> type() {
            return variable.type();
        }

        public <V extends HList<V>> Category<V, A> ccc(Var<V> argument, VarGen vars) {
            if (argument == variable) {
                return (Category<V, A>) new Category.Identity<>(variable.type());
            }
            throw new IllegalStateException("mismatching variables " + this);
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


    record LoadZero<A extends HList<A>>(Var<A>variable) implements Index<A> {

        @Override
        public Type<A> type() {
            return variable.type();
        }

        public <V extends HList<V>> Category<V, A> ccc(Var<V> argument, VarGen vars) {
            if (argument == variable) {
                return (Category<V, A>) new Category.Identity<>(variable.type());
            }
            throw new IllegalStateException("mismatching variables " + this);
        }

        public <V> Index<A> substitute(Var<V> argument, Pass3<V> replacement) {
            throw new UnsupportedOperationException("unimplemented");
        }

        public String toString() {
            return variable.toString();
        }
    }

    record Lambda<A extends HList<A>, B, R>(Type<A>domain, Type<R>range, Args<A, B, R>arguments,
                                            Function<Var<A>, Pass3<B>>f) implements Pass3<R> {
        private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

        public <V> Pass3<R> substitute(Var<V> argument, Pass3<V> replacement) {
            return new Lambda<>(domain, range, arguments, x -> f.apply(x).substitute(argument, replacement));
        }

        public Type<R> type() {
            return range;
        }

        @Override
        public <T extends HList<T>> Category<T, R> ccc(Var<T> argument, VarGen vars) {
            var arg = vars.createArgument(domain);
            var body = f.apply(arg);
            var ccc = body.ccc(arg, vars);

            return Category.makeLambda(argument.type(), range, arguments, ccc);
        }

        public String toString() {
            var depth = DEPTH.get();
            DEPTH.set(depth + 1);

            String str;
            try {
                var dummy = new Var<>(domain, depth);
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

    record Pure<A extends Constable>(Type<A>type, ConstantDesc value) implements Pass3<A> {

        public <V> Pass3<A> substitute(Var<V> argument, Pass3<V> replacement) {
            return this;
        }

        @Override
        public <T extends HList<T>> Category<T, A> ccc(Var<T> argument, VarGen vars) {
            return Category.constant(argument.type(), type, value);
        }

        public Type<A> type() {
            return type;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
}

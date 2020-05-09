package com.sstewartgallus.pass1;

import com.sstewartgallus.ir.Category;
import com.sstewartgallus.ir.VarGen;
import com.sstewartgallus.term.Var;
import com.sstewartgallus.type.*;

import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.util.Objects;
import java.util.function.Function;

public interface Pass3<A> {
    Type<A> type();

    default <V> Pass3<A> substitute(Var<V> argument, Pass3<V> replacement) {
        throw null;
    }

    <T extends HList> Category<T, A> ccc(Var<T> argument, VarGen vars);

    record Apply<A, B>(Pass3<F<A, B>>f, Pass3<A>x) implements Pass3<B> {
        public <V> Pass3<B> substitute(Var<V> argument, Pass3<V> replacement) {
            return new Apply<>(f.substitute(argument, replacement), x.substitute(argument, replacement));
        }

        @Override
        public <T extends HList> Category<T, B> ccc(Var<T> argument, VarGen vars) {
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

    record Head<A, B extends HList>(Type<A>head, Type<B>tail, Pass3<Cons<A, B>>list) implements Pass3<A> {
        public String toString() {
            return "(head " + list + ")";
        }

        public <V> Pass3<A> substitute(Var<V> argument, Pass3<V> replacement) {
            return new Head<>(head, tail, list.substitute(argument, replacement));
        }

        @Override
        public <T extends HList> Category<T, A> ccc(Var<T> argument, VarGen vars) {
            var prod = list.ccc(argument, vars);
            return Category.head(this.head, this.tail).compose(prod);
        }

        @Override
        public Type<A> type() {
            return head;
        }
    }

    record Tail<A, B extends HList>(Type<A>head, Type<B>tail, Pass3<Cons<A, B>>list) implements Pass3<B> {
        public String toString() {
            return "(tail " + list + ")";
        }

        public <V> Pass3<B> substitute(Var<V> argument, Pass3<V> replacement) {
            return new Tail<>(head, tail, list.substitute(argument, replacement));
        }

        @Override
        public <T extends HList> Category<T, B> ccc(Var<T> argument, VarGen vars) {
            var prod = list.ccc(argument, vars);
            return Category.tail(this.head, this.tail).compose(prod);
        }

        @Override
        public Type<B> type() {
            return tail;
        }
    }

    record Load<A>(Var<A>variable) implements Pass3<A> {

        @Override
        public Type<A> type() {
            return variable.type();
        }

        public <V extends HList> Category<V, A> ccc(Var<V> argument, VarGen vars) {
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

    record Lambda<A extends HList, B, R>(Type<A>domain, Args<A, B, R>nesting,
                                            Function<Pass3<A>, Pass3<B>>f) implements Pass3<R> {
        public <V> Pass3<R> substitute(Var<V> argument, Pass3<V> replacement) {
            return new Lambda<>(domain, nesting, x -> f.apply(x).substitute(argument, replacement));
        }

        public Type<R> type() {
            throw null;
            //var range = f.apply(new Load<>(new Var<A>(domain, 0))).type();
            // return new Type.FunType<>(domain, range);
        }

        @Override
        public <T extends HList> Category<T, R> ccc(Var<T> argument, VarGen vars) {
            var tail = argument.type();

            var newArg = vars.createArgument(domain);

            var body = f.apply(new Load<>(newArg));

            var t = Type.cons(domain, tail);
            var list = vars.createArgument(t);

            body = body.substitute(newArg, new Head<>(domain, tail, new Load<>(list)));
            body = body.substitute(argument, new Tail<>(domain, tail, new Load<>(list)));

            throw null;
//            return Category.curry(body.ccc(list, vars));
        }

        public String toString() {
            var depth = DEPTH.get();
            DEPTH.set(depth + 1);

            String str;
            try {
                var dummy = new Var<>(domain, depth);
                var body = f.apply(new Load<>(dummy));

                str = "{" + dummy + ": " + domain + "} -> " + body;
            } finally {
                DEPTH.set(depth);
                if (depth == 0) {
                    DEPTH.remove();
                }
            }
            return str;
        }

        private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);
    }

    record Pure<A extends Constable>(Type<A>type, ConstantDesc value) implements Pass3<A> {

        public <V> Pass3<A> substitute(Var<V> argument, Pass3<V> replacement) {
            return this;
        }

        @Override
        public <T extends HList> Category<T, A> ccc(Var<T> argument, VarGen vars) {
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

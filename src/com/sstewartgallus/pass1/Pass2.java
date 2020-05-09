package com.sstewartgallus.pass1;

import com.sstewartgallus.ir.Category;
import com.sstewartgallus.ir.VarGen;
import com.sstewartgallus.term.Var;
import com.sstewartgallus.type.Cons;
import com.sstewartgallus.type.F;
import com.sstewartgallus.type.HList;
import com.sstewartgallus.type.Type;

import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.util.Objects;
import java.util.function.Function;

public interface Pass2<A> {
    Type<A> type();

    default <V> Pass2<A> substitute(Var<V> argument, Pass2<V> replacement) {
        throw null;
    }

    <T extends HList> Category<T, A> ccc(Var<T> argument, VarGen vars);

    record Apply<A, B>(Pass2<F<A, B>>f, Pass2<A>x) implements Pass2<B> {
        public <V> Pass2<B> substitute(Var<V> argument, Pass2<V> replacement) {
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

    record IfCond<A>(Type<A>t, Pass2<Boolean>cond, Pass2<A>onCond, Pass2<A>elseCond) implements Pass2<A> {
        @Override
        public <A1> Pass2<A> substitute(Var<A1> argument, Pass2<A1> replacement) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public <T extends HList> Category<T, A> ccc(Var<T> argument, VarGen vars) {
            throw null;
        }

        public Type<A> type() {
            return t;
        }

        public String toString() {
            return "{if " + t + " " + cond + " " + onCond + " " + elseCond + "}";
        }
    }

    record Head<A, B extends HList>(Type<A>head, Type<B>tail, Pass2<Cons<A, B>>list) implements Pass2<A> {
        public <V> Pass2<A> substitute(Var<V> argument, Pass2<V> replacement) {
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

    record Tail<A, B extends HList>(Type<A>head, Type<B>tail, Pass2<Cons<A, B>>list) implements Pass2<B> {
        public <V> Pass2<B> substitute(Var<V> argument, Pass2<V> replacement) {
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

    record Load<A>(Var<A>variable) implements Pass2<A> {

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

        public <V> Pass2<A> substitute(Var<V> argument, Pass2<V> replacement) {
            if (argument == variable) {
                return (Pass2<A>) replacement;
            }
            return this;
        }

        public String toString() {
            return variable.toString();
        }
    }

    interface Body<A> {
        <V> Body<A> substitute(Var<V> argument, Pass2<V> replacement);

        Type<A> type();

        <T extends HList> Category<T, A> ccc(Var<T> argument, VarGen vars);
    }


    record Thunk<A>(Body<A>body) implements Pass2<A> {
        @Override
        public Type<A> type() {
            return body.type();
        }

        @Override
        public <V> Pass2<A> substitute(Var<V> argument, Pass2<V> replacement) {
            return new Thunk<>(body.substitute(argument, replacement));
        }

        @Override
        public <T extends HList> Category<T, A> ccc(Var<T> argument, VarGen vars) {
            return body.ccc(argument, vars);
        }

        public String toString() {
            return "(" + body + ")";
        }
    }

    record Expr<A>(Pass2<A>body) implements Body<A> {
        @Override
        public <X> Body<A> substitute(Var<X> argument, Pass2<X> replacement) {
            return new Expr<>(body.substitute(argument, replacement));
        }

        @Override
        public Type<A> type() {
            return body.type();
        }

        @Override
        public <T extends HList> Category<T, A> ccc(Var<T> argument, VarGen vars) {
            return body.ccc(argument, vars);
        }

        public String toString() {
            return body.toString();
        }
    }

    record Lambda<A, B>(Type<A>domain, Function<Pass2<A>, Body<B>>f) implements Body<F<A, B>> {
        public <V> Body<F<A, B>> substitute(Var<V> argument, Pass2<V> replacement) {
            return new Lambda<>(domain, x -> f.apply(x).substitute(argument, replacement));
        }

        public Type<F<A, B>> type() {
            var range = f.apply(new Load<>(new Var<A>(domain, 0))).type();
            return new Type.FunType<>(domain, range);
        }

        @Override
        public <T extends HList> Category<T, F<A, B>> ccc(Var<T> argument, VarGen vars) {
            var tail = argument.type();

            var newArg = vars.createArgument(domain);

            var body = f.apply(new Load<>(newArg));

            var t = Type.cons(domain, tail);
            var list = vars.createArgument(t);;

            body = body.substitute(newArg, new Head<>(domain, tail, new Load<>(list)));
            body = body.substitute(argument, new Tail<>(domain, tail, new Load<>(list)));

            return Category.curry(body.ccc(list, vars));
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

    record Pure<A extends Constable>(Type<A>type, ConstantDesc value) implements Pass2<A> {

        public <V> Pass2<A> substitute(Var<V> argument, Pass2<V> replacement) {
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

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

public interface Pass1<A> {
    Type<A> type();

    default <V> Pass1<A> substitute(Var<V> argument, Pass1<V> replacement) {
        throw null;
    }

    record Apply<A, B>(Pass1<F<A, B>>f, Pass1<A>x) implements Pass1<B> {
        public <V> Pass1<B> substitute(Var<V> argument, Pass1<V> replacement) {
            return new Apply<>(f.substitute(argument, replacement), x.substitute(argument, replacement));
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

    record IfCond<A>(Type<A>t, Pass1<Boolean>cond, Pass1<A>onCond, Pass1<A>elseCond) implements Pass1<A> {
        @Override
        public <A1> Pass1<A> substitute(Var<A1> argument, Pass1<A1> replacement) {
            throw new UnsupportedOperationException("unimplemented");
        }

        public Type<A> type() {
            return t;
        }

        public String toString() {
            return "{if " + t + " " + cond + " " + onCond + " " + elseCond + "}";
        }
    }

    record Head<A, B extends HList>(Type<A>head, Type<B>tail, Pass1<Cons<A, B>>list) implements Pass1<A> {
        public <V> Pass1<A> substitute(Var<V> argument, Pass1<V> replacement) {
            return new Head<>(head, tail, list.substitute(argument, replacement));
        }

        @Override
        public Type<A> type() {
            return head;
        }
    }

    record Tail<A, B extends HList>(Type<A>head, Type<B>tail, Pass1<Cons<A, B>>list) implements Pass1<B> {
        public <V> Pass1<B> substitute(Var<V> argument, Pass1<V> replacement) {
            return new Tail<>(head, tail, list.substitute(argument, replacement));
        }

        @Override
        public Type<B> type() {
            return tail;
        }
    }

    record Load<A>(Var<A>variable) implements Pass1<A> {

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

        public <V> Pass1<A> substitute(Var<V> argument, Pass1<V> replacement) {
            if (argument == variable) {
                return (Pass1<A>) replacement;
            }
            return this;
        }

        public String toString() {
            return variable.toString();
        }
    }

    interface Body<A> {
        <V> Body<A> substitute(Var<V> argument, Pass1<V> replacement);
    }

    record Expr<A>(Pass1<A>body) implements Body<A> {
        @Override
        public <X> Body<A> substitute(Var<X> argument, Pass1<X> replacement) {
            return new Expr<>(body.substitute(argument, replacement));
        }

        public String toString() {
            return body.toString();
        }
    }

    record Capture<A, B>(Pass1<A>capture, Function<Pass1<A>, Body<B>>f) implements Body<B> {

        public String toString() {
            return "{" + rightShow() + "}";
        }

        private String rightShow() {
            var depth = DEPTH.get();
            DEPTH.set(depth + 1);

            String str;
            try {
                var dummy = new Var<>(capture.type(), depth);
                var body = f.apply(new Load<>(dummy));
                String bodyStr;
                if (body instanceof Capture<?, ?> app) {
                    bodyStr = app.rightShow();
                } else {
                    bodyStr = body.toString();
                }

                str = "{" + dummy + " = " + capture + "} -> " + bodyStr;
            } finally {
                DEPTH.set(depth);
                if (depth == 0) {
                    DEPTH.remove();
                }
            }
            return str;
        }

        private static final ThreadLocal<Integer> DEPTH = Lambda.DEPTH;

        @Override
        public <X> Body<B> substitute(Var<X> argument, Pass1<X> replacement) {
            return new Capture<>(capture.substitute(argument, replacement), x -> f.apply(x).substitute(argument, replacement));
        }
    }

    record Lambda<A, B>(Type<A>domain, Function<Pass1<A>, Body<B>>f) implements Pass1<F<A, B>> {
        public <V> Pass1<F<A, B>> substitute(Var<V> argument, Pass1<V> replacement) {
            return new Lambda<>(domain, x -> f.apply(x).substitute(argument, replacement));
        }

        public Type<F<A, B>> type() {
            throw null;
            // var range = f.apply(new Var<>(domain, 0)).type();
            //return new Type.FunType<>(domain, range);
        }

        public String toString() {
            return "{" + rightShow() + "}";
        }

        private String rightShow() {
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

    record Pure<A extends Constable>(Type<A>type, ConstantDesc value) implements Pass1<A> {

        public <V> Pass1<A> substitute(Var<V> argument, Pass1<V> replacement) {
            return this;
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

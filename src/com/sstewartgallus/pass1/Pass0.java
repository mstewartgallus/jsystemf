package com.sstewartgallus.pass1;

import com.sstewartgallus.ir.VarGen;
import com.sstewartgallus.term.Term;
import com.sstewartgallus.term.Var;
import com.sstewartgallus.type.*;

import java.lang.constant.ConstantDesc;
import java.util.Objects;
import java.util.function.Function;

public interface Pass0<L> {
    // fixme... use the visitor pattern to do this passing safely...
    static <T> Pass0<T> from(Term<T> term, VarGen vars) {
        return term.visit(new Term.Visitor<>() {
            @Override
            public Pass0<T> onPure(Type<T> type, ConstantDesc constantDesc) {
                return new Pure<>(type, constantDesc);
            }

            @Override
            public Pass0<T> onLoad(Var<T> variable) {
                return new Load<>(variable);
            }

            @Override
            public <A> Pass0<T> onApply(Term<F<A, T>> f, Term<A> x) {
                return new Apply<>(from(f, vars), from(x, vars));
            }

            @Override
            public <A, B> Pass0<T> onLambda(Equality<T, F<A, B>> equality, Type<A> domain, Function<Term<A>, Term<B>> f) {
                var v = vars.createArgument(domain);
                var body = from(f.apply(new Term.Load<>(v)), vars);
                Pass0<F<A, B>> lambda = new Pass0.Lambda<>(domain, x -> body.substitute(v, x));
                // fixme.. penguin
                return (Pass0) lambda;
            }
        });
    }

    Pass1<L> aggregateLambdas(VarGen vars);

    Type<L> type();

    default <X> Pass0<L> substitute(Var<X> variable, Pass0<X> replacement) {
        throw new UnsupportedOperationException(getClass().toString());
    }

    record Pure<A>(Type<A>type, ConstantDesc value) implements Pass0<A> {
        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @Override
        public <X> Pass0<A> substitute(Var<X> variable, Pass0<X> replacement) {
            return this;
        }

        @Override
        public Pass1<A> aggregateLambdas(VarGen vars) {
            return new Pass1.Pure<>(type, value);
        }
    }

    record Load<A>(Var<A>variable) implements Pass0<A> {
        @Override
        public Type<A> type() {
            return variable.type();
        }

        @Override
        public String toString() {
            return variable.toString();
        }

        @Override
        public <X> Pass0<A> substitute(Var<X> variable, Pass0<X> replacement) {
            if (this.variable == variable) {
                return (Pass0) replacement;
            }
            return this;
        }

        @Override
        public Pass1<A> aggregateLambdas(VarGen vars) {
            return new Pass1.Load<>(variable);
        }
    }

    record Apply<A, B>(Pass0<F<A, B>>f, Pass0<A>x) implements Pass0<B> {
        @Override
        public Type<B> type() {
            var funType = ((Type.FunType<A, B>) f.type());
            var t = x.type();
            if (!Objects.equals(t, funType.domain())) {
                throw new RuntimeException("type error");
            }
            return funType.range();
        }

        @Override
        public String toString() {
            return "(" + f + " " + x + ")";
        }

        @Override
        public <X> Pass0<B> substitute(Var<X> variable, Pass0<X> replacement) {
            return new Apply<>(f.substitute(variable, replacement), x.substitute(variable, replacement));
        }

        @Override
        public Pass1<B> aggregateLambdas(VarGen vars) {
            return new Pass1.Apply<>(f.aggregateLambdas(vars), x.aggregateLambdas(vars));
        }
    }

    record Lambda<A, B>(Type<A>domain, Function<Pass0<A>, Pass0<B>>f) implements Pass0<F<A, B>> {
        private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

        @Override
        public <X> Pass0<F<A, B>> substitute(Var<X> variable, Pass0<X> replacement) {
            return new Lambda<>(domain, x -> f.apply(x).substitute(variable, replacement));
        }

        public Pass1<F<A, B>> aggregateLambdas(VarGen vars) {
            var v = vars.createArgument(domain);
            var body = f.apply(new Load<>(v)).aggregateLambdas(vars);

            if (body instanceof Pass1.Thunk<B> thunk) {
                var expr = thunk.body();
                return new Pass1.Thunk<>(new Pass1.Lambda<>(domain, x -> expr.substitute(v, x)));
            }

            return new Pass1.Thunk<>(new Pass1.Lambda<>(domain, x -> new Pass1.Expr<>(body.substitute(v, x))));
        }

        public Type<F<A, B>> type() {
            var range = f.apply(new Load<>(new Var<>(domain, 0))).type();
            return new Type.FunType<>(domain, range);
        }

        public String toString() {
            var depth = DEPTH.get();
            DEPTH.set(depth + 1);

            String str;
            try {
                var dummy = new Load<>(new Var<>(domain, depth));
                var body = f.apply(dummy);
                String bodyStr = body.toString();

                str = "({" + dummy + ": " + domain + "} → " + bodyStr + ")";
            } finally {
                DEPTH.set(depth);
                if (depth == 0) {
                    DEPTH.remove();
                }
            }
            return str;
        }
    }

    record TypeApply<A, B>(Pass0<V<A, B>>f, Type<A>x) implements Pass0<B> {
        @Override
        public Type<B> type() {
            return ((Type.Forall<A, B>) f.type()).f().apply(x);
        }

        @Override
        public String toString() {
            return "{" + f + " " + x + "}";
        }

        @Override
        public Pass1<B> aggregateLambdas(VarGen vars) {
            throw new UnsupportedOperationException("not yet implemented");
        }
    }

    record Forall<A, B>(Function<Type<A>, Pass0<B>>f) implements Pass0<V<A, B>> {
        @Override
        public Type<V<A, B>> type() {
            return new Type.Forall<>(x -> f.apply(x).type());
        }

        @Override
        public String toString() {
            var dummy = new Type.Var<A>(0);
            return "{forall " + dummy + ". " + f.apply(dummy) + "}";
        }

        @Override
        public Pass1<V<A, B>> aggregateLambdas(VarGen vars) {
            throw new UnsupportedOperationException("not yet implemented");
        }
    }

    record Exists<A, B>(Type<A>x, Pass0<B>y) implements Pass0<E<A, B>> {
        @Override
        public Type<E<A, B>> type() {
            return new Type.Exists<>(x, y.type());
        }

        @Override
        public String toString() {
            return "{exists " + x + ". " + y + "}";
        }

        @Override
        public Pass1<E<A, B>> aggregateLambdas(VarGen vars) {
            throw new UnsupportedOperationException("not yet implemented");
        }
    }
}
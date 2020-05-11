package com.sstewartgallus.pass1;

import com.sstewartgallus.term.Term;
import com.sstewartgallus.term.Var;
import com.sstewartgallus.term.VarGen;
import com.sstewartgallus.type.*;

import java.lang.constant.ConstantDesc;
import java.util.Objects;
import java.util.function.Function;

public interface Pass0<L> {
    static <T> Pass0<T> from(Term<T> term, VarGen vars) {
        return term.visit(new Term.Visitor<>() {
            @Override
            public Pass0<T> onPure(Type<T> type, ConstantDesc constantDesc) {
                return new Pure<>(TPass0.from(type, vars), constantDesc);
            }

            @Override
            public Pass0<T> onLoad(Type<T> type, Var<T> variable) {
                return new Load<>(TPass0.from(type, vars), variable);
            }

            @Override
            public <A> Pass0<T> onApply(Term<F<A, T>> f, Term<A> x) {
                return new Apply<>(from(f, vars), from(x, vars));
            }

            @Override
            public <A, B> Pass0<T> onLambda(Equality<T, F<A, B>> equality, Type<A> domain, Function<Term<A>, Term<B>> f) {
                var d0 = TPass0.from(domain, vars);
                var v = vars.<A>createArgument();
                var body = from(f.apply(new Term.Load<>(domain, v)), vars);
                Pass0<F<A, B>> lambda = new Pass0.Lambda<>(d0, x -> body.substitute(v, x));
                // fixme.. penguin
                return (Pass0) lambda;
            }
        });
    }

    Pass1<L> aggregateLambdas(VarGen vars);

    TPass0<L> type();

    default <X> Pass0<L> substitute(Var<X> variable, Pass0<X> replacement) {
        throw new UnsupportedOperationException(getClass().toString());
    }

    record Pure<A>(TPass0<A>type, ConstantDesc value) implements Pass0<A> {
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

    record Load<A>(TPass0<A>type, Var<A>variable) implements Pass0<A> {
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
            return new Pass1.Load<>(type, variable);
        }
    }

    record Apply<A, B>(Pass0<F<A, B>>f, Pass0<A>x) implements Pass0<B> {
        @Override
        public TPass0<B> type() {
            var funTPass0 = ((TPass0.FunType<A, B>) f.type());
            var t = x.type();
            if (!Objects.equals(t, funTPass0.domain())) {
                throw new RuntimeException("TPass0 error");
            }
            return funTPass0.range();
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

    record Lambda<A, B>(TPass0<A>domain, Function<Pass0<A>, Pass0<B>>f) implements Pass0<F<A, B>> {
        private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

        @Override
        public <X> Pass0<F<A, B>> substitute(Var<X> variable, Pass0<X> replacement) {
            return new Lambda<>(domain, x -> f.apply(x).substitute(variable, replacement));
        }

        public Pass1<F<A, B>> aggregateLambdas(VarGen vars) {
            var v = vars.<A>createArgument();
            var body = f.apply(new Load<>(domain, v)).aggregateLambdas(vars);

            if (body instanceof Pass1.Thunk<B> thunk) {
                var expr = thunk.body();
                return new Pass1.Thunk<>(new Pass1.Lambda<>(domain, x -> expr.substitute(v, x)));
            }

            return new Pass1.Thunk<>(new Pass1.Lambda<>(domain, x -> new Pass1.Expr<>(body.substitute(v, x))));
        }

        public TPass0<F<A, B>> type() {
            var range = f.apply(new Load<>(domain, new Var<>(0))).type();
            return new TPass0.FunType<>(domain, range);
        }

        public String toString() {
            var depth = DEPTH.get();
            DEPTH.set(depth + 1);

            String str;
            try {
                var dummy = new Load<>(domain, new Var<>(depth));
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

    record TPass0Apply<A, B>(Pass0<V<A, B>>f, TPass0<A>x) implements Pass0<B> {
        @Override
        public TPass0<B> type() {
            return ((TPass0.Forall<A, B>) f.type()).f().apply(x);
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

    record Forall<A, B>(Function<TPass0<A>, Pass0<B>>f) implements Pass0<V<A, B>> {
        @Override
        public TPass0<V<A, B>> type() {
            return new TPass0.Forall<>(x -> f.apply(x).type());
        }

        @Override
        public String toString() {
            throw null;
            //   var dummy = new TVar<A>(0);
            //     return "{forall " + dummy + ". " + f.apply(dummy) + "}";
        }

        @Override
        public Pass1<V<A, B>> aggregateLambdas(VarGen vars) {
            throw new UnsupportedOperationException("not yet implemented");
        }
    }

    record Exists<A, B>(TPass0<A>x, Pass0<B>y) implements Pass0<E<A, B>> {
        @Override
        public TPass0<E<A, B>> type() {
            return new TPass0.Exists<>(x, y.type());
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
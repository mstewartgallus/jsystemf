package com.sstewartgallus.pass1;

import com.sstewartgallus.ir.VarGen;
import com.sstewartgallus.term.Var;
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

    default Pass3<A> tuple(VarGen vars) {
        throw new UnsupportedOperationException(getClass().toString());
    }

    interface Body<A> {
        <V> Body<A> substitute(Var<V> argument, Pass2<V> replacement);

        Type<A> type();

        Results<? extends HList<?>, ?, A> tuple(VarGen vars);
    }

    record Apply<A, B>(Pass2<F<A, B>>f, Pass2<A>x) implements Pass2<B> {
        public Pass3<B> tuple(VarGen vars) {
            return new Pass3.Apply<>(f.tuple(vars), x.tuple(vars));
        }

        public <V> Pass2<B> substitute(Var<V> argument, Pass2<V> replacement) {
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

    record Load<A>(Var<A>variable) implements Pass2<A> {
        public Pass3<A> tuple(VarGen vars) {
            return new Pass3.Load<>(variable);
        }

        @Override
        public Type<A> type() {
            return variable.type();
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

    record Results<L extends HList<L>, R, A>(Type<L>type,
                                             Args<L, R, A>proof,
                                             Function<Pass3.Get<?, L>, Pass3<R>>f) {
        public Pass3.Lambda<L, R, A> lambda(Type<A> range) {
            return new Pass3.Lambda<>(type, range, proof, x -> f.apply(new Pass3.Get<>(x, new Index.Zip<>(type))));
        }

    }

    record Thunk<A>(Body<A>body) implements Pass2<A> {
        public Pass3<A> tuple(VarGen vars) {
            return body.tuple(vars).lambda(body.type());
        }

        @Override
        public Type<A> type() {
            return body.type();
        }

        @Override
        public <V> Pass2<A> substitute(Var<V> argument, Pass2<V> replacement) {
            return new Thunk<>(body.substitute(argument, replacement));
        }

        public String toString() {
            return "(" + body + ")";
        }
    }

    record Expr<A>(Pass2<A>body) implements Body<A> {
        public Results<? extends HList<?>, ?, A> tuple(VarGen vars) {
            var bodyTuple = body.tuple(vars);
            return new Results<>(Type.nil(), new Args.Zero<>(), nil -> bodyTuple);
        }

        @Override
        public <X> Body<A> substitute(Var<X> argument, Pass2<X> replacement) {
            return new Expr<>(body.substitute(argument, replacement));
        }

        @Override
        public Type<A> type() {
            return body.type();
        }


        public String toString() {
            return body.toString();
        }
    }

    record Lambda<A, B>(Type<A>domain,
                        Function<Pass2<A>, Body<B>>f) implements Body<F<A, B>> {
        private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

        public Results<? extends HList<?>, ?, F<A, B>> tuple(VarGen vars) {
            var v = vars.createArgument(domain);
            var body = f.apply(new Load<>(v));
            var bodyTuple = body.tuple(vars);
            return consArgument(v, bodyTuple);
        }

        public <L extends HList<L>, R> Results<HList.Cons<A, L>, R, F<A, B>> consArgument(Var<A> v, Results<L, R, B> bodyTuple) {
            var tail = bodyTuple.type;
            var proof = bodyTuple.proof;
            var f = bodyTuple.f;
            return new Results<>(Type.cons(domain, tail), new Args.Add<>(domain, proof),
                    argList -> helper(v, f, argList));
        }

        public <Q extends HList<Q>, L extends HList<L>, R> Pass3<R> helper(Var<A> v, Function<Pass3.Get<?, L>, Pass3<R>> f, Pass3.Get<Q, HList.Cons<A, L>> argList) {
            Index<Q, L> next = new Index.Next<>(argList.ix());
            return f.apply(new Pass3.Get<>(argList.variable(), next)).substitute(v, new Pass3.WrapGet<>(argList));
        }

        public <V> Body<F<A, B>> substitute(Var<V> argument, Pass2<V> replacement) {
            return new Lambda<>(domain, x -> f.apply(x).substitute(argument, replacement));
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
                var dummy = new Var<>(domain, depth);
                var body = f.apply(new Load<>(dummy));

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

    record Pure<A extends Constable>(Type<A>type, ConstantDesc value) implements Pass2<A> {
        public Pass3<A> tuple(VarGen vars) {
            return new Pass3.Pure<>(type, value);
        }

        public <V> Pass2<A> substitute(Var<V> argument, Pass2<V> replacement) {
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

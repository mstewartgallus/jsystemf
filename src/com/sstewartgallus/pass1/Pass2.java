package com.sstewartgallus.pass1;

import com.sstewartgallus.term.Id;
import com.sstewartgallus.term.VarGen;
import com.sstewartgallus.type.F;
import com.sstewartgallus.type.HList;

import java.util.Objects;
import java.util.function.Function;

public interface Pass2<A> {
    TPass0<A> type();

    <V> Pass2<A> substitute(Id<V> argument, Pass2<V> replacement);

    Pass3<A> uncurry(VarGen vars);

    interface Body<A> {
        <V> Body<A> substitute(Id<V> argument, Pass2<V> replacement);

        TPass0<A> type();

        Results<? extends HList<?>, ?, A> tuple(VarGen vars);
    }

    record Apply<A, B>(Pass2<F<A, B>>f, Pass2<A>x) implements Pass2<B> {
        public Pass3<B> uncurry(VarGen vars) {
            return new Pass3.Apply<>(f.uncurry(vars), x.uncurry(vars));
        }

        public <V> Pass2<B> substitute(Id<V> argument, Pass2<V> replacement) {
            return new Apply<>(f.substitute(argument, replacement), x.substitute(argument, replacement));
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

    record Results<L extends HList<L>, R, A>(TPass0<L>type,
                                             Args<L, R, A>proof,
                                             Function<Pass3.Get<?, L>, Pass3<R>>f) {
        public Pass3.Lambda<L, R, A> lambda(TPass0<A> range) {
            return new Pass3.Lambda<>(range, type, proof, x -> f.apply(new Pass3.Get<>(type, x, new Index.Zip<>(type))));
        }

    }

    record Thunk<A>(Body<A>body) implements Pass2<A> {
        public Pass3<A> uncurry(VarGen vars) {
            return body.tuple(vars).lambda(body.type());
        }

        @Override
        public TPass0<A> type() {
            return body.type();
        }

        @Override
        public <V> Pass2<A> substitute(Id<V> argument, Pass2<V> replacement) {
            return new Thunk<>(body.substitute(argument, replacement));
        }

        public String toString() {
            return "(" + body + ")";
        }
    }

    record Expr<A>(Pass2<A>body) implements Body<A> {
        public Results<? extends HList<?>, ?, A> tuple(VarGen vars) {
            var bodyTuple = body.uncurry(vars);
            return new Results<>(TPass0.NilType.NIL, new Args.Zero<>(), nil -> bodyTuple);
        }

        @Override
        public <X> Body<A> substitute(Id<X> argument, Pass2<X> replacement) {
            return new Expr<>(body.substitute(argument, replacement));
        }

        @Override
        public TPass0<A> type() {
            return body.type();
        }


        public String toString() {
            return body.toString();
        }
    }

    record Lambda<A, B>(TPass0<A>domain,
                        Function<Pass2<A>, Body<B>>f) implements Body<F<A, B>> {
        private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

        public Results<? extends HList<?>, ?, F<A, B>> tuple(VarGen vars) {
            var v = vars.<A>createId();
            var body = f.apply(new Var<>(domain, v));
            var bodyTuple = body.tuple(vars);
            return consArgument(v, bodyTuple);
        }

        public <L extends HList<L>, R> Results<HList.Cons<A, L>, R, F<A, B>> consArgument(Id<A> v, Results<L, R, B> bodyTuple) {
            var tail = bodyTuple.type;
            var proof = bodyTuple.proof;
            var f = bodyTuple.f;
            return new Results<>(new TPass0.ConsType<>(domain, tail), new Args.Add<>(domain, proof),
                    argList -> helper(v, f, argList));
        }

        public <Q extends HList<Q>, L extends HList<L>, R> Pass3<R> helper(Id<A> v, Function<Pass3.Get<?, L>, Pass3<R>> f, Pass3.Get<Q, HList.Cons<A, L>> argList) {
            Index<Q, L> next = new Index.Next<>(argList.ix());
            return f.apply(new Pass3.Get<>(argList.type(), argList.variable(), next)).substitute(v, new Pass3.WrapGet<>(argList));
        }

        public <V> Body<F<A, B>> substitute(Id<V> argument, Pass2<V> replacement) {
            return new Lambda<>(domain, x -> f.apply(x).substitute(argument, replacement));
        }

        public TPass0<F<A, B>> type() {
            var range = f.apply(new Var<>(domain, new Id<>(0))).type();
            return new TPass0.FunType<>(domain, range);
        }

        public String toString() {
            var depth = DEPTH.get();
            DEPTH.set(depth + 1);

            String str;
            try {
                var dummy = new Id<A>(depth);
                var body = f.apply(new Var<>(domain, dummy));

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

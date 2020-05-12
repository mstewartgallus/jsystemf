package com.sstewartgallus.pass1;

import com.sstewartgallus.plato.*;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface Pass1<L> {
    static <A, B> Pass1<V<A, B>> v(Function<TPass0<A>, Pass1<B>> f) {
        return new Forall<>(f);
    }

    private static <A> Set<A> union(Set<A> left, Set<A> right) {
        var x = new TreeSet<>(left);
        x.addAll(right);
        return x;
    }

    static <T> Pass1<T> from(Term<T> term, IdGen vars) {
        if (term instanceof PureValue<T> pure) {
            return new Pure<T>(TPass0.from(pure.type(), vars), pure.value());
        }

        if (term instanceof VarThunk<T> load) {
            return new Var<>(TPass0.from(load.type(), vars), load.variable());
        }

        if (term instanceof ApplyThunk<?, T> apply) {
            return fromApply(apply, vars);
        }

        if (term instanceof CurriedLambdaValue<T> lambda) {
            // fixme... penguin
            return new Thunk<>(fromLambda(lambda, vars));
        }
        throw new IllegalArgumentException("unexpected term " + term);
    }

    static <A, B> Pass1<B> fromApply(ApplyThunk<A, B> apply, IdGen vars) {
        return new Apply<>(from(apply.f(), vars), from(apply.x(), vars));
    }

    static <T> Body<T> fromLambda(CurriedLambdaValue<T> lambda, IdGen vars) {
        var body = lambda.body();
        return fromBody(body, vars);
    }

    static <T> Body<T> fromBody(CurriedLambdaValue.Body<T> body, IdGen vars) {
        if (body instanceof CurriedLambdaValue.MainBody<T> mainBody) {
            return new Expr<T>(from(mainBody.body(), vars));
        }
        var lambdaBody = (CurriedLambdaValue.LambdaBody<?, ?>) body;
        return (Body) fromLambda(lambdaBody, vars);
    }

    static <A, B> Lambda<A, B> fromLambda(CurriedLambdaValue.LambdaBody<A, B> lambdaBody, IdGen ids) {
        var domain = TPass0.from(lambdaBody.domain(), ids);
        var v = ids.<A>createId();
        var body = lambdaBody.f().apply(new VarThunk<>(lambdaBody.domain(), v));
        var processedBody = fromBody(body, ids);
        return new Lambda<A, B>(domain, x -> processedBody.substitute(v, x));
    }

    <A> Pass1<L> substitute(Id<A> argument, Pass1<A> replacement);

    Results<L> captureEnv(IdGen vars);

    TPass0<L> type();

    interface Body<A> {
        <V> Pass1.Body<A> substitute(Id<V> argument, Pass1<V> replacement);

        TPass0<A> type();

        BodyResults<A> captureEnv(IdGen vars);
    }

    record Results<L>(Set<Var<?>>captured, Pass2<L>value) {
    }

    record Apply<A, B>(Pass1<F<A, B>>f, Pass1<A>x) implements Pass1<B> {
        public Results<B> captureEnv(IdGen vars) {
            var fCapture = f.captureEnv(vars);
            var xCapture = x.captureEnv(vars);

            var captures = union(fCapture.captured, xCapture.captured);

            return new Results<>(captures, new Pass2.Apply<>(fCapture.value, xCapture.value));
        }


        public <V> Pass1<B> substitute(Id<V> argument, Pass1<V> replacement) {
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
            return "{" + f + " " + x + "}";
        }
    }

    record BodyResults<L>(Set<Var<?>>captured, Pass2.Body<L>value) {
    }

    record Thunk<A>(Body<A>body) implements Pass1<A> {
        private static <A> Pass2<A> helper(List<Var<?>> free, int ii, Pass2.Body<A> body) {
            if (ii >= free.size()) {
                return new Pass2.Thunk<>(body);
            }
            return helper(free, ii, free.get(ii), body);
        }

        private static <A, B> Pass2<A> helper(List<Var<?>> free, int ii, Var<B> freeVar, Pass2.Body<A> body) {
            return new Pass2.Apply<>(helper(free, ii + 1, new Pass2.Lambda<>(freeVar.type(), x -> body.substitute(freeVar.variable(), x))),
                    freeVar);
        }

        public Results<A> captureEnv(IdGen vars) {
            var results = body.captureEnv(vars);
            var captured = new TreeSet<>(results.captured);

            List<Var<?>> free = captured.stream().sorted().collect(Collectors.toUnmodifiableList());

            var chunk = results.value;
            return new Results<>(captured, helper(free, 0, chunk));
        }

        @Override
        public TPass0<A> type() {
            return body.type();
        }

        @Override
        public <V> Pass1<A> substitute(Id<V> argument, Pass1<V> replacement) {
            return new Thunk<>(body.substitute(argument, replacement));
        }

        public String toString() {
            return "(" + body + ")";
        }
    }

    record Expr<A>(Pass1<A>body) implements Pass1.Body<A> {
        @Override
        public <X> Pass1.Body<A> substitute(Id<X> argument, Pass1<X> replacement) {
            return new Pass1.Expr<>(body.substitute(argument, replacement));
        }

        @Override
        public TPass0<A> type() {
            return body.type();
        }

        @Override
        public BodyResults<A> captureEnv(IdGen vars) {
            var results = body.captureEnv(vars);
            return new BodyResults<>(results.captured, new Pass2.Expr<>(results.value));
        }

        public String toString() {
            return body.toString();
        }
    }

    record Lambda<A, B>(TPass0<A>domain, Function<Pass1<A>, Body<B>>f) implements Body<F<A, B>> {
        private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

        public <V> Pass1.Body<F<A, B>> substitute(Id<V> argument, Pass1<V> replacement) {
            return new Pass1.Lambda<>(domain, x -> f.apply(x).substitute(argument, replacement));
        }

        public TPass0<F<A, B>> type() {
            var range = f.apply(new Var<>(domain, new Id<>(0))).type();
            return new TPass0.FunType<>(domain, range);
        }

        @Override
        public BodyResults<F<A, B>> captureEnv(IdGen vars) {
            var v = vars.<A>createId();
            var load = new Var<>(domain, v);
            var body = f.apply(load);
            var results = body.captureEnv(vars);
            Set<Var<?>> captures = new TreeSet<>(results.captured);
            captures.remove(load);

            var chunk = results.value;
            return new BodyResults<>(captures, new Pass2.Lambda<>(domain, x -> chunk.substitute(v, x)));
        }

        public String toString() {
            var depth = DEPTH.get();
            DEPTH.set(depth + 1);

            String str;
            try {
                var dummy = new Id<A>(depth);
                var body = f.apply(new Var<>(domain, dummy));

                str = "(" + dummy + ": " + domain + ") → " + body;
            } finally {
                DEPTH.set(depth);
                if (depth == 0) {
                    DEPTH.remove();
                }
            }
            return str;
        }
    }

    record Forall<A, B>(Function<TPass0<A>, Pass1<B>>f) implements Pass1<V<A, B>> {
        public TPass0<V<A, B>> type() {
            return new TPass0.Forall<>(x -> f.apply(x).type());
        }

        @Override
        public <X> Pass1<V<A, B>> substitute(Id<X> argument, Pass1<X> replacement) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public Results<V<A, B>> captureEnv(IdGen vars) {
            throw new UnsupportedOperationException("unimplemented");
        }

        public String toString() {
            throw new UnsupportedOperationException("unimplemented");
            // var dummy = new TVar<A>(0);
            //   return "{forall " + dummy + ". " + f.apply(new Load<>(dummy)) + "}";
        }
    }
}
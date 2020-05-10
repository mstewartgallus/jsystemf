package com.sstewartgallus.pass1;

import com.sstewartgallus.ir.VarGen;
import com.sstewartgallus.term.Var;
import com.sstewartgallus.type.E;
import com.sstewartgallus.type.F;
import com.sstewartgallus.type.Type;
import com.sstewartgallus.type.V;

import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

// https://gitlab.haskell.org/ghc/ghc/-/wikis/commentary/compiler/core-syn-type
// arguments CoreExpr = Expr Var

// data Expr b	-- "b" for the arguments of binders,
//   = Var	  Id
//   | Lit   Literal
//   | App   (Expr b) (Arg b)
//   | Lam   b (Expr b)
//   | Let   (Bind b) (Expr b)
//   | Case  (Expr b) b Type [Alt b]
//   | Cast  (Expr b) Coercion
//   | Tick  (Tickish Id) (Expr b)
//   | Type  Type

// arguments Arg b = Expr b
// arguments Alt b = (AltCon, [b], Expr b)

// data AltCon = DataAlt DataCon | LitAlt  Literal | DEFAULT

// data Bind b = NonRec b (Expr b) | Rec [(b, (Expr b))]


// https://github.com/DanBurton/Blog/blob/master/Literate%20Haskell/SystemF.lhs

// fixme... move types out of the ir package
public interface Pass1<L> {
    static <A, B> Pass1<V<A, B>> v(Function<Type<A>, Pass1<B>> f) {
        return new Forall<>(f);
    }

    private static <A> Set<A> union(Set<A> left, Set<A> right) {
        var x = new TreeSet<>(left);
        x.addAll(right);
        return x;
    }

    <A> Pass1<L> substitute(Var<A> argument, Pass1<A> replacement);

    default Results<L> captureEnv(VarGen vars) {
        throw null;
    }

    Type<L> type();

    interface Body<A> {
        <V> Pass1.Body<A> substitute(Var<V> argument, Pass1<V> replacement);

        Type<A> type();

        BodyResults<A> captureEnv(VarGen vars);
    }

    record Results<L>(Set<Var<?>>captured, Pass2<L>value) {
    }

    record Load<A>(Var<A>variable) implements Pass1<A> {
        public Results<A> captureEnv(VarGen vars) {
            return new Results<A>(Set.of(variable), new Pass2.Load<>(variable));
        }

        @Override
        public Type<A> type() {
            return variable.type();
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

    record Apply<A, B>(Pass1<F<A, B>>f, Pass1<A>x) implements Pass1<B> {
        public Results<B> captureEnv(VarGen vars) {
            var fCapture = f.captureEnv(vars);
            var xCapture = x.captureEnv(vars);

            var captures = union(fCapture.captured, xCapture.captured);

            return new Results<>(captures, new Pass2.Apply<>(fCapture.value, xCapture.value));
        }


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
            return new Pass2.Apply<>(helper(free, ii + 1, new Pass2.Lambda<>(freeVar.type(), x -> body.substitute(freeVar, x))),
                    new Pass2.Load<>(freeVar));
        }

        public Results<A> captureEnv(VarGen vars) {
            var results = body.captureEnv(vars);
            var captured = new TreeSet<>(results.captured);

            List<Var<?>> free = captured.stream().sorted().collect(Collectors.toUnmodifiableList());

            var chunk = results.value;
            return new Results<A>(captured, helper(free, 0, chunk));
        }

        @Override
        public Type<A> type() {
            return body.type();
        }

        @Override
        public <V> Pass1<A> substitute(Var<V> argument, Pass1<V> replacement) {
            return new Thunk<>(body.substitute(argument, replacement));
        }

        public String toString() {
            return "(" + body + ")";
        }
    }

    record Expr<A>(Pass1<A>body) implements Pass1.Body<A> {
        @Override
        public <X> Pass1.Body<A> substitute(Var<X> argument, Pass1<X> replacement) {
            return new Pass1.Expr<>(body.substitute(argument, replacement));
        }

        @Override
        public Type<A> type() {
            return body.type();
        }

        @Override
        public BodyResults<A> captureEnv(VarGen vars) {
            var results = body.captureEnv(vars);
            return new BodyResults<>(results.captured, new Pass2.Expr<>(results.value));
        }

        public String toString() {
            return body.toString();
        }
    }

    record Lambda<A, B>(Type<A>domain, Function<Pass1<A>, Body<B>>f) implements Body<F<A, B>> {
        private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

        public <V> Pass1.Body<F<A, B>> substitute(Var<V> argument, Pass1<V> replacement) {
            return new Pass1.Lambda<>(domain, x -> f.apply(x).substitute(argument, replacement));
        }

        public Type<F<A, B>> type() {
            var range = f.apply(new Pass1.Load<>(new Var<A>(domain, 0))).type();
            return new Type.FunType<>(domain, range);
        }

        @Override
        public BodyResults<F<A, B>> captureEnv(VarGen vars) {
            var v = vars.createArgument(domain);
            var body = f.apply(new Load<>(v));
            var results = body.captureEnv(vars);
            Set<Var<?>> captures = new TreeSet<>(results.captured);
            captures.remove(v);

            var chunk = results.value;
            return new BodyResults<F<A, B>>(captures, new Pass2.Lambda<A, B>(domain, x -> chunk.substitute(v, x)));
        }

        public String toString() {
            var depth = DEPTH.get();
            DEPTH.set(depth + 1);

            String str;
            try {
                var dummy = new Var<>(domain, depth);
                var body = f.apply(new Pass1.Load<>(dummy));

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


    record Pure<A extends Constable>(Type<A>type, ConstantDesc value) implements Pass1<A> {
        public Results<A> captureEnv(VarGen vars) {
            return new Results<>(Set.of(), new Pass2.Pure<>(type, value));
        }

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

    record TypeApply<A, B>(Pass1<V<A, B>>f, Type<A>x) implements Pass1<B> {

        @Override
        public <A> Pass1<B> substitute(Var<A> argument, Pass1<A> replacement) {
            throw new UnsupportedOperationException("unimplemented");
        }

        public Type<B> type() {
            return ((Type.Forall<A, B>) f.type()).f().apply(x);
        }

        @Override
        public String toString() {
            return "{" + f + " " + x + "}";
        }

    }

    record Forall<A, B>(Function<Type<A>, Pass1<B>>f) implements Pass1<V<A, B>> {
        public Type<V<A, B>> type() {
            return new Type.Forall<>(x -> f.apply(x).type());
        }

        @Override
        public <X> Pass1<V<A, B>> substitute(Var<X> argument, Pass1<X> replacement) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public String toString() {
            var dummy = new Type.Var<A>(0);
            return "{forall " + dummy + ". " + f.apply(dummy) + "}";
        }
    }

    record Exists<A, B>(Type<A>x, Pass1<B>y) implements Pass1<E<A, B>> {
        public Type<E<A, B>> type() {
            return new Type.Exists<>(x, y.type());
        }

        @Override
        public <X> Pass1<E<A, B>> substitute(Var<X> argument, Pass1<X> replacement) {
            throw new UnsupportedOperationException("unimplemented");
        }

        public String toString() {
            return "{exists " + x + ". " + y + "}";
        }
    }
}
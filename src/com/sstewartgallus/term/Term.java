package com.sstewartgallus.term;

import com.sstewartgallus.ir.VarGen;
import com.sstewartgallus.pass1.Pass1;
import com.sstewartgallus.pass1.Pass2;
import com.sstewartgallus.type.E;
import com.sstewartgallus.type.F;
import com.sstewartgallus.type.Type;
import com.sstewartgallus.type.V;

import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

// https://gitlab.haskell.org/ghc/ghc/-/wikis/commentary/compiler/core-syn-type
// argument CoreExpr = Expr Var

// data Expr b	-- "b" for the argument of binders,
//   = Var	  Id
//   | Lit   Literal
//   | App   (Expr b) (Arg b)
//   | Lam   b (Expr b)
//   | Let   (Bind b) (Expr b)
//   | Case  (Expr b) b Type [Alt b]
//   | Cast  (Expr b) Coercion
//   | Tick  (Tickish Id) (Expr b)
//   | Type  Type

// argument Arg b = Expr b
// argument Alt b = (AltCon, [b], Expr b)

// data AltCon = DataAlt DataCon | LitAlt  Literal | DEFAULT

// data Bind b = NonRec b (Expr b) | Rec [(b, (Expr b))]


// https://github.com/DanBurton/Blog/blob/master/Literate%20Haskell/SystemF.lhs

// fixme... move types out of the ir package
public interface Term<L> {
    static <A, B> Term<B> apply(Term<V<A, B>> f, Type<A> x) {
        return new TypeApply<>(f, x);
    }

    static <A, B> Term<B> apply(Term<F<A, B>> f, Term<A> x) {
        return new Apply<>(f, x);
    }

    static <A, B> Term<V<A, B>> v(Function<Type<A>, Term<B>> f) {
        return new Forall<>(f);
    }

    static <A> Term<A> ifCond(Type<A> t, Term<Boolean> cond, Term<A> onCond, Term<A> elseCond) {
        return new IfCond<>(t, cond, onCond, elseCond);
    }

    static <T extends Constable> Term<T> pure(Type<T> type, T value) {
        var constant = value.describeConstable();
        if (constant.isEmpty()) {
            throw new IllegalArgumentException("not a constant value " + value);
        }
        return new Pure<>(type, constant.get());
    }

    <A> Term<L> substitute(Var<A> argument, Term<A> replacement);

    default Pass1<L> aggregateLambdas(VarGen vars) {
        throw new UnsupportedOperationException(getClass().toString());
    }

    Type<L> type();

    record Load<A>(Var<A>variable) implements Term<A> {
        public Pass1<A> aggregateLambdas(VarGen vars) {
            return new Pass1.Load<>(variable);
        }

        @Override
        public Type<A> type() {
            return variable.type();
        }

        public <V> Term<A> substitute(com.sstewartgallus.term.Var<V> argument, Term<V> replacement) {
            if (argument == variable) {
                return (Term<A>) replacement;
            }
            return this;
        }

        public String toString() {
            return variable.toString();
        }
    }

    record Apply<A, B>(Term<F<A, B>>f, Term<A>x) implements Term<B> {
        public Pass1<B> aggregateLambdas(VarGen vars) {
            return new Pass1.Apply<>(f.aggregateLambdas(vars), x.aggregateLambdas(vars));
        }


        public <V> Term<B> substitute(Var<V> argument, Term<V> replacement) {
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
            return "(" + f + " " + x + ")";
        }
    }

    record IfCond<A>(Type<A>t, Term<Boolean>cond, Term<A>onCond, Term<A>elseCond) implements Term<A> {

        @Override
        public <A1> Term<A> substitute(Var<A1> argument, Term<A1> replacement) {
            throw new UnsupportedOperationException("unimplemented");
        }

        public Type<A> type() {
            return t;
        }

        public String toString() {
            return "{if " + t + " " + cond + " " + onCond + " " + elseCond + "}";
        }
    }

    record Lambda<A, B>(Type<A>domain, Function<Term<A>, Term<B>>f) implements Term<F<A, B>> {
        private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

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

        public Pass1<F<A, B>> aggregateLambdas(VarGen vars) {
            var v = vars.createArgument(domain);
            var body = f.apply(new Load<>(v)).aggregateLambdas(vars);

            if (body instanceof Pass1.Thunk<B> thunk) {
                var expr = thunk.body();
                return new Pass1.Thunk<>(new Pass1.Lambda<>(domain, x -> expr.substitute(v, x)));
            }

            return new Pass1.Thunk<>(new Pass1.Lambda<>(domain, x -> new Pass1.Expr<>(body.substitute(v, x))));
        }

        public <V> Term<F<A, B>> substitute(Var<V> argument, Term<V> replacement) {
            return new Lambda<>(domain, arg -> f.apply(arg).substitute(argument, replacement));
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

    record Pure<A extends Constable>(Type<A>type, ConstantDesc value) implements Term<A> {
        public Pass1<A> aggregateLambdas(VarGen vars) {
            return new Pass1.Pure<>(type, value);
        }

        public <V> Term<A> substitute(Var<V> argument, Term<V> replacement) {
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

    record TypeApply<A, B>(Term<V<A, B>>f, Type<A>x) implements Term<B> {

        @Override
        public <A> Term<B> substitute(Var<A> argument, Term<A> replacement) {
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

    record Forall<A, B>(Function<Type<A>, Term<B>>f) implements Term<V<A, B>> {
        public Type<V<A, B>> type() {
            return new Type.Forall<>(x -> f.apply(x).type());
        }

        @Override
        public <X> Term<V<A, B>> substitute(Var<X> argument, Term<X> replacement) {
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

    record Exists<A, B>(Type<A>x, Term<B>y) implements Term<E<A, B>> {
        public Type<E<A, B>> type() {
            return new Type.Exists<>(x, y.type());
        }

        @Override
        public <X> Term<E<A, B>> substitute(Var<X> argument, Term<X> replacement) {
            throw new UnsupportedOperationException("unimplemented");
        }

        public String toString() {
            return "{exists " + x + ". " + y + "}";
        }
    }
}
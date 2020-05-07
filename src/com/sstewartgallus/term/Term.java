package com.sstewartgallus.term;

import com.sstewartgallus.Category;
import com.sstewartgallus.pass2.VarGen;
import com.sstewartgallus.type.*;

import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
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

    // fixme... type check
    default Category<Void, L> ccc() {
        var vars = new VarGen();
        return ccc(vars.createArgument(Type.VOID), vars);
    }

    // fixme... should be for internal use only...
    <A> Category<A, L> ccc(Var<A> argument, VarGen vars);

    // fixme... should be for internal use only... somehow (maybe Stack wakling?)
    <A> Term<L> substitute(Var<A> argument, Term<A> replacement);

    record Intrinsic<A, B>(Category<A, B>f) implements Term<F<A, B>> {
        public <V> Category<V, F<A, B>> ccc(Var<V> argument, VarGen vars) {
            Category<T<V, A>, A> snd = Category.second(argument.type(), f.domain());
            return Category.curry(f.compose(snd));
        }

        public <V> Term<F<A, B>> substitute(Var<V> argument, Term<V> replacement) {
            return this;
        }

        @Override
        public Type<F<A, B>> type() {
            return f.domain().to(f.range());
        }
    }

    record Var<A>(Type<A>type, int number) implements Term<A>, Comparable<Var<?>> {

        public <V> Category<V, A> ccc(Var<V> argument, VarGen vars) {
            if (argument == this) {
                return (Category<V, A>) new Category.Identity<>(type);
            }
            throw new IllegalStateException("mismatching variables " + this);
        }

        public <V> Term<A> substitute(Var<V> argument, Term<V> replacement) {
            if (argument == this) {
                return (Term<A>) replacement;
            }
            return this;
        }

        public String toString() {
            return "v" + number();
        }

        @Override
        public int compareTo(Var<?> var) {
            return var.number() - number();
        }
    }

    static <T extends Constable> Term<T> pure(Type<T> type, T value) {
        var constant = value.describeConstable();
        if (constant.isEmpty()) {
            throw new IllegalArgumentException("not a constant value " + value);
        }
        return new Pure<>(type, constant.get());
    }

    Type<L> type();

    record Apply<A, B>(Term<F<A, B>>f, Term<A>x) implements Term<B> {

        public <V> Term<B> substitute(Var<V> argument, Term<V> replacement) {
            return new Apply<>(f.substitute(argument, replacement), x.substitute(argument, replacement));
        }

        public <V> Category<V, B> ccc(Var<V> argument, VarGen vars) {
            Category<V, F<A, B>> fCcc = f.ccc(argument, vars);
            Category<V, A> xCcc = x.ccc(argument, vars);
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
    record IfCond<A>(Type<A>t, Term<Boolean>cond, Term<A>onCond, Term<A>elseCond) implements Term<A> {


        @Override
        public <A1> Category<A1, A> ccc(Var<A1> argument, VarGen vars) {
            throw new UnsupportedOperationException("unimplemented");
        }

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

    record First<A, B>(Type<A>left, Type<B>right, Term<T<A, B>>product) implements Term<A> {

        public <V> Term<A> substitute(Var<V> argument, Term<V> replacement) {
            return new First<>(left, right, product.substitute(argument, replacement));
        }

        @Override
        public <V> Category<V, A> ccc(Var<V> argument, VarGen vars) {
            Category<V, T<A, B>> prod = product.ccc(argument, vars);
            return Category.first(this.left, this.right).compose(prod);
        }


        @Override
        public Type<A> type() {
            return left;
        }

    }

    record Second<A, B>(Type<A>left, Type<B>right, Term<T<A, B>>product) implements Term<B> {

        public <V> Term<B> substitute(Var<V> argument, Term<V> replacement) {
            return new Second<>(left, right, product.substitute(argument, replacement));
        }

        @Override
        public <V> Category<V, B> ccc(Var<V> argument, VarGen vars) {
            Category<V, T<A, B>> prod = product.ccc(argument, vars);
            return Category.second(this.left, this.right).compose(prod);
        }

        @Override
        public Type<B> type() {
            return right;
        }

    }

    // fixme... define equality properly...
    record Lambda<A, B>(Type<A>domain, Function<Term<A>, Term<B>>f) implements Term<F<A, B>> {

        public <V> Term<F<A, B>> substitute(Var<V> argument, Term<V> replacement) {
            return new Lambda<>(domain, arg -> f.apply(arg).substitute(argument, replacement));
        }

        public <V> Category<V, F<A, B>> ccc(Var<V> argument, VarGen vars) {
            var left = argument.type();
            var t = left.and(domain);
            var arg = vars.createArgument(t);

            var body = f.apply(new Second<>(left, domain, arg))
                    .substitute(argument, new First<>(left, domain, arg))
                    .ccc(arg, vars);
            return Category.curry(body);
        }

        public Type<F<A, B>> type() {
            var range = f.apply(new Var<>(domain, 0)).type();
            return new Type.FunType<>(domain, range);
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
                var body = f.apply(dummy);
                String bodyStr;
                if (body instanceof Lambda<?, ?> app) {
                    bodyStr = app.rightShow();
                } else {
                    bodyStr = body.toString();
                }

                str = "{" + dummy + ": " + domain + "} -> " + bodyStr;
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

    record Pure<A extends Constable>(Type<A>type, ConstantDesc value) implements Term<A> {

        public <V> Term<A> substitute(Var<V> argument, Term<V> replacement) {
            return this;
        }

        public <V> Category<V, A> ccc(Var<V> argument, VarGen vars) {
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

    record TypeApply<A, B>(Term<V<A, B>>f, Type<A>x) implements Term<B> {

        @Override
        public <A> Category<A, B> ccc(Var<A> argument, VarGen vars) {
            throw new UnsupportedOperationException("unimplemented");
        }

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

        public <X> Category<X, V<A, B>> ccc(Var<X> argument, VarGen vars) {
            // fixme... sending mutable state through here...
            return new Category.Forall<>(argument.type, x -> f.apply(x).ccc(argument, vars));
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
        public Category<Void, E<A, B>> ccc() {
            throw new UnsupportedOperationException("unimplemented");
        }

        public <X> Category<X, E<A, B>> ccc(Var<X> argument, VarGen vars) {
            return new Category.Exists<>(x, y.ccc(argument, vars));
        }

        @Override
        public <X> Term<E<A, B>> substitute(Var<X> argument, Term<X> replacement) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public String toString() {
            return "{exists " + x + ". " + y + "}";
        }
    }
}
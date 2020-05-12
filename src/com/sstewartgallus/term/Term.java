package com.sstewartgallus.term;

import com.sstewartgallus.type.*;

import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.util.Objects;
import java.util.function.Function;

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

/**
 * The high level syntax for a System F term in my little language.
 * <p>
 * This is intended to be pristine source language untainted by compiler stuff.
 * <p>
 * Any processing should happen AFTER this step.
 *
 * @param <L>
 */
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

    static <A> Term<A> ifCond(Type<A> type, Term<Boolean> cond, Term<A> onCond, Term<A> elseCond) {
        return new IfCond<>(type, cond, onCond, elseCond);
    }

    static <T extends Constable> Term<T> pure(Type<T> type, T value) {
        var constant = value.describeConstable();
        if (constant.isEmpty()) {
            throw new IllegalArgumentException("not a constable value " + value);
        }
        return new Pure<>(type, constant.get());
    }

    Type<L> type() throws TypeCheckException;

    <X> X visit(Visitor<X, L> visitor);

    interface Visitor<X, L> {
        X onPure(Type<L> type, ConstantDesc constantDesc);

        X onLoad(Type<L> type, Id<L> variable);

        <A> X onApply(Term<F<A, L>> f, Term<A> x);

        <A, B> X onLambda(Equality<L, F<A, B>> equality, Type<A> domain, Function<Term<A>, Term<B>> f);
    }

    record Pure<A>(Type<A>type, ConstantDesc value) implements Term<A> {
        public Pure {
            Objects.requireNonNull(type);
            Objects.requireNonNull(value);
        }

        @Override
        public String toString() {
            return value.toString();
        }

        @Override
        public <X> X visit(Visitor<X, A> visitor) {
            return visitor.onPure(type, value);
        }
    }

    record Load<A>(Type<A>type, Id<A>variable) implements Term<A> {
        public Load {
            Objects.requireNonNull(variable);
        }

        @Override
        public String toString() {
            return "v" + variable.toString();
        }

        @Override
        public <X> X visit(Visitor<X, A> visitor) {
            return visitor.onLoad(type, variable);
        }
    }

    record Apply<A, B>(Term<F<A, B>>f, Term<A>x) implements Term<B> {
        public Apply {
            Objects.requireNonNull(f);
            Objects.requireNonNull(x);
        }

        @Override
        public Type<B> type() throws TypeCheckException {
            var fType = f.type();

            var funType = (Type.FunType<A, B>) fType;
            var range = funType.range();

            var argType = x.type();

            fType.unify(argType.to(range));

            return funType.range();
        }

        @Override
        public String toString() {
            return "(" + f + " " + x + ")";
        }

        @Override
        public <X> X visit(Visitor<X, B> visitor) {
            return visitor.onApply(f, x);
        }
    }

    record Lambda<A, B>(Type<A>domain, Function<Term<A>, Term<B>>f) implements Term<F<A, B>> {
        private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

        public Lambda {
            Objects.requireNonNull(domain);
            Objects.requireNonNull(f);
        }

        @Override
        public Type<F<A, B>> type() throws TypeCheckException {
            var range = f.apply(new Load<>(domain, new Id<>(0))).type();
            return new Type.FunType<>(domain, range);
        }

        @Override
        public String toString() {
            var depth = DEPTH.get();
            DEPTH.set(depth + 1);

            String str;
            try {
                var dummy = new Load<>(domain, new Id<>(depth));
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

        @Override
        public <X> X visit(Visitor<X, F<A, B>> visitor) {
            return visitor.onLambda(new Equality.Identical<>(), domain, f);
        }
    }

    record TypeApply<A, B>(Term<V<A, B>>f, Type<A>x) implements Term<B> {
        public TypeApply {
            Objects.requireNonNull(f);
            Objects.requireNonNull(x);
        }

        @Override
        public <X> X visit(Visitor<X, B> visitor) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public Type<B> type() throws TypeCheckException {
            return ((Type.Forall<A, B>) f.type()).f().apply(x);
        }

        @Override
        public String toString() {
            return "{" + f + " " + x + "}";
        }
    }

    record Forall<A, B>(Function<Type<A>, Term<B>>f) implements Term<V<A, B>> {
        public Forall {
            Objects.requireNonNull(f);
        }

        @Override
        public <X> X visit(Visitor<X, V<A, B>> visitor) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public Type<V<A, B>> type() throws TypeCheckException {
            // fixme... pass in the variable generator?
            var v = new Id<A>(0);
            var body = f.apply(new Type.Load<>(v)).type();
            return Type.v(x -> body.substitute(v, x));
        }

        @Override
        public String toString() {
            var dummy = new Type.Load<>(new Id<A>(0));
            return "{forall " + dummy + ". " + f.apply(dummy) + "}";
        }
    }

    record Exists<A, B>(Type<A>x, Term<B>y) implements Term<E<A, B>> {
        public Exists {
            Objects.requireNonNull(x);
            Objects.requireNonNull(y);
        }

        @Override
        public <X> X visit(Visitor<X, E<A, B>> visitor) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public Type<E<A, B>> type() throws TypeCheckException {
            return new Type.Exists<>(x, y.type());
        }

        @Override
        public String toString() {
            return "{exists " + x + ". " + y + "}";
        }
    }

    record IfCond<A>(Type<A>type, Term<Boolean>cond, Term<A>onCond, Term<A>elseCond) implements Term<A> {
        @Override
        public String toString() {
            return "{if " + type + " " + cond + " " + onCond + " " + elseCond + "}";
        }

        @Override
        public <X> X visit(Visitor<X, A> visitor) {
            throw new UnsupportedOperationException("unimplemented");
        }
    }
}
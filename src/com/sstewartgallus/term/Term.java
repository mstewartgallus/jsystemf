package com.sstewartgallus.term;

import com.sstewartgallus.ir.Category;
import com.sstewartgallus.ir.VarGen;
import com.sstewartgallus.pass1.Pass1;
import com.sstewartgallus.type.*;

import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    default Category<Nil, L> ccc() {
        var vars = new VarGen();
        return ccc(vars.createArgument(Type.nil()), vars);
    }

    // fixme... should be for internal use only...
    <A extends HList> Category<A, L> ccc(Var<A> argument, VarGen vars);

    // fixme... should be for internal use only... somehow (maybe Stack wakling?)
    <A> Term<L> substitute(Var<A> argument, Term<A> replacement);

    record Results<L>(Set<Var<?>>captured, Pass1<L>value) {
    }

    default Results<L> captures(VarGen vars) {
        throw null;
    }

    static <T extends Constable> Term<T> pure(Type<T> type, T value) {
        var constant = value.describeConstable();
        if (constant.isEmpty()) {
            throw new IllegalArgumentException("not a constant value " + value);
        }
        return new Pure<>(type, constant.get());
    }

    Type<L> type();

    private static <A> Set<A> union(Set<A> left, Set<A> right) {
        var x = new TreeSet<>(left);
        x.addAll(right);
        return x;
    }

    record Load<A>(Var<A>variable) implements Term<A> {
        public Results<A> captures(VarGen vars) {
            return new Results<A>(Set.of(variable), new Pass1.Load<>(variable));
        }

        @Override
        public Type<A> type() {
            return variable.type();
        }

        public <V extends HList> Category<V, A> ccc(com.sstewartgallus.term.Var<V> argument, VarGen vars) {
            if (argument == variable) {
                return (Category<V, A>) new Category.Identity<>(variable.type());
            }
            throw new IllegalStateException("mismatching variables " + this);
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
        public Results<B> captures(VarGen vars) {
            var fCapture = f.captures(vars);
            var xCapture = x.captures(vars);

            var captures = union(fCapture.captured, xCapture.captured);

            return new Results<>(captures, new Pass1.Apply<>(fCapture.value, xCapture.value));
        }


        public <V> Term<B> substitute(Var<V> argument, Term<V> replacement) {
            return new Apply<>(f.substitute(argument, replacement), x.substitute(argument, replacement));
        }

        public <V extends HList> Category<V, B> ccc(Var<V> argument, VarGen vars) {
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
        public <A1 extends HList> Category<A1, A> ccc(Var<A1> argument, VarGen vars) {
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

    record Head<A, B extends HList>(Type<A>head, Type<B>tail, Term<Cons<A, B>>list) implements Term<A> {

        public <V> Term<A> substitute(Var<V> argument, Term<V> replacement) {
            return new Head<>(head, tail, list.substitute(argument, replacement));
        }

        @Override
        public <V extends HList> Category<V, A> ccc(Var<V> argument, VarGen vars) {
            Category<V, Cons<A, B>> prod = list.ccc(argument, vars);
            return Category.head(this.head, this.tail).compose(prod);
        }


        @Override
        public Type<A> type() {
            return head;
        }

    }

    record Tail<A, B extends HList>(Type<A>head, Type<B>tail, Term<Cons<A, B>>list) implements Term<B> {

        public <V> Term<B> substitute(Var<V> argument, Term<V> replacement) {
            return new Tail<>(head, tail, list.substitute(argument, replacement));
        }

        @Override
        public <V extends HList> Category<V, B> ccc(Var<V> argument, VarGen vars) {
            Category<V, Cons<A, B>> prod = list.ccc(argument, vars);
            return Category.tail(this.head, this.tail).compose(prod);
        }

        @Override
        public Type<B> type() {
            return tail;
        }

    }

    // fixme... define equality properly...
    record Lambda<A, B>(Type<A>domain, Function<Term<A>, Term<B>>f) implements Term<F<A, B>> {
        public Results<F<A, B>> captures(VarGen vars) {
            var v = vars.createArgument(domain);
            var body = f.apply(new Load<>(v));

            var results = body.captures(vars);
            var captured = new TreeSet<>(results.captured);
            captured.remove(v);

            List<Var<?>> free = captured.stream().sorted().collect(Collectors.toUnmodifiableList());

            var chunk = results.value;
            return new Results<F<A, B>>(captured, new Pass1.Lambda<A, B>(domain, x -> setEnv(free, 0,
                    new Pass1.Expr<>(chunk.substitute(v, x)))));
        }

        private static <A> Pass1.Body<A> setEnv(List<Var<?>> free, int ii, Pass1.Body<A> body) {
            if (ii >= free.size()) {
                return body;
            }
            return helper(free, ii, free.get(ii), body);
        }

        private static <A, B> Pass1.Body<A> helper(List<Var<?>> free, int ii,Var<B> freeVar, Pass1.Body<A> body) {
            return new Pass1.Capture<B, A>(new Pass1.Load<B>(freeVar), replacement -> setEnv(free, ii + 1, body.substitute(freeVar, replacement)));
        }

        public <V> Term<F<A, B>> substitute(Var<V> argument, Term<V> replacement) {
            return new Lambda<>(domain, arg -> f.apply(arg).substitute(argument, replacement));
        }

        public <V extends HList> Category<V, F<A, B>> ccc(Var<V> argument, VarGen vars) {
            var tail = argument.type();


            var newArg = vars.createArgument(domain);

            var body = f.apply(new Load<>(newArg));

            var t = Type.cons(domain, tail);
            var list = vars.createArgument(t);

            body = body.substitute(newArg, new Head<>(domain, tail, new Load<>(list)));
            body = body.substitute(argument, new Tail<>(domain, tail, new Load<>(list)));

            return Category.curry(body.ccc(list, vars));
        }

        public Type<F<A, B>> type() {
            var range = f.apply(new Load<>(new Var<>(domain, 0))).type();
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
                var dummy = new Load<>(new Var<>(domain, depth));
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
        public Results<A> captures(VarGen vars) {
            return new Results<>(Set.of(), new Pass1.Pure<>(type, value));
        }

        public <V> Term<A> substitute(Var<V> argument, Term<V> replacement) {
            return this;
        }

        public <V extends HList> Category<V, A> ccc(Var<V> argument, VarGen vars) {
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
        public <A extends HList> Category<A, B> ccc(Var<A> argument, VarGen vars) {
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

        public <X extends HList> Category<X, V<A, B>> ccc(Var<X> argument, VarGen vars) {
            // fixme... sending mutable state through here...
            return new Category.Forall<>(argument.type(), x -> f.apply(x).ccc(argument, vars));
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

        public <X extends HList> Category<X, E<A, B>> ccc(Var<X> argument, VarGen vars) {
            return new Category.Exists<>(x, y.ccc(argument, vars));
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
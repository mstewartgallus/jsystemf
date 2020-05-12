package com.sstewartgallus.runtime;

import com.sstewartgallus.term.Id;
import com.sstewartgallus.term.Term;
import com.sstewartgallus.type.Equality;
import com.sstewartgallus.type.F;
import com.sstewartgallus.type.Type;

import java.lang.constant.ConstantDesc;
import java.util.function.Function;

/**
 * This will be a simple obviously correct interpreter.
 * <p>
 * This shall form the bootstrap to a metacircular approach where we interpret the JIT and then JIT the interpreter and
 * the JIT.
 */
public final class Interpreter {
    private Interpreter() {
    }

    // fixme... consider using a callsite/ linking API
    private static final Term.Visitor PENGUIN = getVisitor();

    // fixme... should output a Value?
    public static <A> Term<A> normalize(Term<A> term) {
        return term.visit((Term.Visitor<Term<A>, A>) PENGUIN);
    }

    private static <A> Term.Visitor<Term<A>, A> getVisitor() {
        return new Term.Visitor<>() {

            @Override
            public Term<A> onPure(Type<A> type, ConstantDesc constantDesc) {
                return new Term.Pure<>(type, constantDesc);
            }

            @Override
            public Term<A> onLoad(Type<A> type, Id<A> variable) {
                return new Term.Load<>(type, variable);
            }

            @Override
            public <X> Term<A> onApply(Term<F<X, A>> f, Term<X> x) {
                // fixme... allow for tail recursion....
                var fNorm = (Term.Lambda<X, A>) normalize(f);
                // lmaoo..
                return normalize(fNorm.f().apply(x));
            }

            @Override
            public <A1, B> Term<A> onLambda(Equality<A, F<A1, B>> equality, Type<A1> domain, Function<Term<A1>, Term<B>> f) {
                // fixme...
                return (Term) new Term.Lambda<>(domain, f);
            }
        };
    }
}

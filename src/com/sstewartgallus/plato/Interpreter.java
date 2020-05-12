package com.sstewartgallus.plato;

/**
 * This will be a simple obviously correct interpreter.
 * <p>
 * This shall form the bootstrap to a metacircular approach where we interpret the JIT and then JIT the interpreter and
 * the JIT.
 */
public final class Interpreter {
    private Interpreter() {
    }

    public static <A> Term<A> normalize(Term<A> term) {
        while (term instanceof ThunkTerm<A> thunk) {
            if (term instanceof ApplyThunk<?, A> apply) {
                term = evalApply(apply);
                continue;
            }
            if (term instanceof TypeApplyThunk<?, A> apply) {
                term = evalTypeApply(apply);
                continue;
            }
            // fixme... might want to have a checked exception here...
            throw new IllegalStateException("unexpected term " + thunk);
        }
        return term;
    }

    private static <A, B> Term<B> evalApply(ApplyThunk<A, B> apply) {
        var f = apply.f();
        var x = apply.x();
        // fixme... type check?

        var fNorm = (LambdaValue<A, B>) normalize(f);
        // fixme... should I normalize the argument?
        return fNorm.f().apply(x);
    }

    private static <A, B> Term<B> evalTypeApply(TypeApplyThunk<A, B> apply) {
        var f = apply.f();
        // fixme... do types need to be normalized as well?
        var x = apply.x();
        // fixme... type check?

        var fNorm = (TypeLambdaTerm<A, B>) normalize(f);
        // fixme... should I normalize the argument?
        return fNorm.f().apply(x);
    }
}

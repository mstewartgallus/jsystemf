package com.sstewartgallus.plato;

import com.sstewartgallus.ext.pointfree.CallThunk;
import com.sstewartgallus.ext.pointfree.ConstantThunk;
import com.sstewartgallus.ext.pointfree.IdentityThunk;
import com.sstewartgallus.ext.variables.VarValue;

import java.util.Objects;

import static java.lang.invoke.MethodHandles.lookup;

public record ApplyThunk<A, B>(Term<F<A, B>>f, Term<A>x) implements ThunkTerm<B>, LambdaTerm<B> {
    private static final TermInvoker INVOKE_TERM = com.sstewartgallus.runtime.TermInvoker.newInstance(lookup(), TermInvoker.class);

    public ApplyThunk {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    private static <X, A, B> Term<F<X, B>> call(Term<F<X, F<A, B>>> fValue, Term<F<X, A>> xValue) {
        var fType = (FunctionType<X, F<A, B>>) fValue.type();
        var fRange = (FunctionType<A, B>) fType.range();

        var z = fType.domain();
        var a = fRange.domain();
        var b = fRange.range();
        return Term.apply(Term.apply(Term.apply(Term.apply(Term.apply(new CallThunk<>(), z), a), b), fValue), xValue);
    }

    public Term<B> visitChildren(Visitor visitor) {
        return Term.apply(visitor.term(f), visitor.term(x));
    }

    @Override
    public <X> Term<F<X, B>> pointFree(VarValue<X> varValue) {
        var fValue = f.pointFree(varValue);
        var xValue = x.pointFree(varValue);

        if (fValue instanceof IdentityThunk) {
            return (Term) xValue;
        }

        if (fValue instanceof ApplyThunk<?, F<X, F<A, B>>> apply) {
            if (apply.f() instanceof ConstantThunk) {
                return (Term) apply.x();
            }
        }

        return call(fValue, xValue);
    }

    @Override
    public Type<B> type() throws TypeCheckException {
        var fType = f.type();

        var funType = (FunctionType<A, B>) fType;
        var range = funType.range();

        var argType = x.type();

        fType.unify(argType.to(range));

        return funType.range();
    }

    @Override
    public String toString() {
        return "(" + noBrackets() + ")";
    }

    private String noBrackets() {
        if (f instanceof ApplyThunk<?, F<A, B>> fApply) {
            return fApply.noBrackets() + " " + x;
        }
        return f + " " + x;
    }

    @Override
    public Term<B> stepThunk() {
        // fixme... unfortunately we need to involve dynalink in this even for very typical objects..
        // fixme... consider an environment parameter with a lookup() at least..
        System.err.println(f + " " + x);
        return INVOKE_TERM.apply(f, x);
    }

    @FunctionalInterface
    public interface TermInvoker {
        <A, B> Term<B> apply(Term<F<A, B>> f, Term<A> x);
    }

}

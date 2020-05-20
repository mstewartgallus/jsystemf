package com.sstewartgallus.plato;

import com.sstewartgallus.ext.pretty.PrettyThunk;
import com.sstewartgallus.ext.variables.VarValue;

import java.util.Objects;

public abstract class LambdaValue<A, B> implements ValueTerm<F<A, B>>, LambdaTerm<F<A, B>> {
    private final Type<A> domain;

    public LambdaValue(Type<A> domain) {
        Objects.requireNonNull(domain);
        this.domain = domain;
    }

    public final Type<A> domain() {
        return domain;
    }

    public abstract Term<B> apply(Term<A> x);

    @Override
    public final Term<F<A, B>> visitChildren(Visitor visitor) {
        var v = new VarValue<>(domain());
        var body = visitor.term(apply(v));
        return new SimpleLambdaValue<>(visitor.type(domain()), x -> v.substituteIn(body, x));
    }

    @Override
    public final <X> Term<F<X, F<A, B>>> pointFree(VarValue<X> varValue) {
        var v = new VarValue<>(domain());
        var body = apply(v);
        return body.pointFree(v).pointFree(varValue);
    }

    @Override
    public final Type<F<A, B>> type() throws TypeCheckException {
        try (var pretty = PrettyThunk.generate(domain())) {
            var range = apply(pretty).type();
            return new FunctionType<>(domain(), range);
        }
    }

    @Override
    public final String toString() {
        return "(" + noBrackets() + ")";
    }

    private String noBrackets() {
        try (var pretty = PrettyThunk.generate(domain())) {
            var body = apply(pretty);
            if (body instanceof LambdaValue<?, ?> lambdaValue) {
                return "λ (" + pretty + " " + domain() + ") " + lambdaValue.noBrackets();
            }
            return "λ (" + pretty + " " + domain() + ") " + body;
        }
    }

}

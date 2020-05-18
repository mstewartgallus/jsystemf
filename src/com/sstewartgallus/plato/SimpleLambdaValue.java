package com.sstewartgallus.plato;

import com.sstewartgallus.ext.pretty.PrettyThunk;
import com.sstewartgallus.ext.variables.VarValue;

import java.util.Objects;
import java.util.function.Function;

public final class SimpleLambdaValue<A, B> extends LambdaValue<A, B> implements ValueTerm<F<A, B>>, LambdaTerm<F<A, B>> {
    private final Function<Term<A>, Term<B>> f;

    public SimpleLambdaValue(Type<A> domain,
                             Function<Term<A>, Term<B>> f) {
        super(domain);
        Objects.requireNonNull(f);
        this.f = f;
    }


    public Term<F<A, B>> visitChildren(Visitor visitor) {
        var v = new VarValue<>(domain());
        var body = visitor.term(f.apply(v));
        return new SimpleLambdaValue<>(visitor.type(domain()), x -> v.substituteIn(body, x));
    }

    @Override
    public <X> Term<F<X, F<A, B>>> pointFree(VarValue<X> varValue) {
        var v = new VarValue<>(domain());
        var body = f.apply(v);
        return body.pointFree(v).pointFree(varValue);
    }

    @Override
    public Type<F<A, B>> type() throws TypeCheckException {
        try (var pretty = PrettyThunk.generate(domain())) {
            var range = f.apply(pretty).type();
            return new FunctionType<>(domain(), range);
        }
    }

    @Override
    public String toString() {
        return "(" + noBrackets() + ")";
    }

    private String noBrackets() {
        try (var pretty = PrettyThunk.generate(domain())) {
            var body = f.apply(pretty);
            if (body instanceof SimpleLambdaValue<?, ?> lambdaValue) {
                return "λ (" + pretty + " " + domain() + ") " + lambdaValue.noBrackets();
            }
            return "λ (" + pretty + " " + domain() + ") " + body;
        }
    }

    public Term<B> apply(Term<A> x) {
//        System.err.println("Apply " + this + " " + x);

        // fixme... typecheck domain?
        return f.apply(x);
    }
}

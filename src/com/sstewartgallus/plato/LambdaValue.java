package com.sstewartgallus.plato;

import com.sstewartgallus.ext.pretty.PrettyValue;

import java.util.Objects;
import java.util.function.Function;

public record LambdaValue<A, B>(Type<A>domain,
                                Function<Term<A>, Term<B>>f) implements ValueTerm<F<A, B>>, CoreTerm<F<A, B>> {
    public LambdaValue {
        Objects.requireNonNull(domain);
        Objects.requireNonNull(f);
    }

    public Term<F<A, B>> visitChildren(Visitor visitor) {
        return new LambdaValue<>(domain, x -> visitor.term(f.apply(x)));
    }

    @Override
    public Type<F<A, B>> type() throws TypeCheckException {
        try (var pretty = PrettyValue.generate(domain)) {
            var range = f.apply(pretty).type();
            return new FunctionType<>(domain, range);
        }
    }

    @Override
    public String toString() {
        try (var pretty = PrettyValue.generate(domain)) {
            var body = f.apply(pretty);
            return "({" + pretty + ": " + domain + "} → " + body + ")";
        }
    }

    public Term<B> apply(Term<A> x) {
        // fixme... typecheck domain?
        return f.apply(x);
    }
}

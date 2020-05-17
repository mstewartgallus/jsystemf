package com.sstewartgallus.plato;

import com.sstewartgallus.ext.pretty.PrettyType;

import java.util.Objects;
import java.util.function.Function;

public record TypeLambdaValue<A, B>(Function<Type<A>, Term<B>>f) implements ValueTerm<V<A, B>>, LambdaTerm<V<A, B>> {
    public TypeLambdaValue {
        Objects.requireNonNull(f);
    }

    @Override
    public Term<V<A, B>> visitChildren(Visitor visitor) {
        return new TypeLambdaValue<>(x -> visitor.term(f.apply(x)));
    }

    @Override
    public Type<V<A, B>> type() {
        return Type.v(x -> f.apply(x).type());
    }

    public Term<B> apply(Type<A> x) {
        System.err.println(this + " " + x);
        var result = f.apply(x);
        System.err.println("Result " + result);
        return result;
    }

    @Override
    public String toString() {
        try (var pretty = PrettyType.<A>generate()) {
            var body = f.apply(pretty);
            return "(∀" + pretty + " → " + body + ")";
        }
    }
}

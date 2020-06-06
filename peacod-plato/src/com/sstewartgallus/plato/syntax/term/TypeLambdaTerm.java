package com.sstewartgallus.plato.syntax.term;

import com.sstewartgallus.plato.runtime.V;
import com.sstewartgallus.plato.syntax.type.Type;
import com.sstewartgallus.plato.syntax.type.TypeCheckException;

import java.util.Objects;
import java.util.function.Function;

public class TypeLambdaTerm<A, B> implements Term<V<A, B>> {
    private final Function<Type<A>, Term<B>> f;

    TypeLambdaTerm(Function<Type<A>, Term<B>> f) {
        this.f = Objects.requireNonNull(f);
    }

    @Override
    public Term<V<A, B>> visitChildren(Visitor visitor) {
        var self = this;
        return new TypeLambdaTerm<>(x -> visitor.term(self.apply(x)));
    }

    @Override
    public Type<V<A, B>> type() {
        var self = this;
        return Type.v(x -> self.apply(x).type());
    }

    @Override
    public Constraints findConstraints() throws TypeCheckException {
        throw null;
    }

    public Term<B> apply(Type<A> x) {
        return f.apply(x);
    }
}

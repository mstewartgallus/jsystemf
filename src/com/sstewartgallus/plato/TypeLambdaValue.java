package com.sstewartgallus.plato;

public abstract class TypeLambdaValue<A, B> implements ValueTerm<V<A, B>>, LambdaTerm<V<A, B>> {
    @Override
    public Term<V<A, B>> visitChildren(Visitor visitor) {
        var self = this;
        return new SimpleTypeLambdaValue<>(x -> visitor.term(self.apply(x)));
    }

    @Override
    public Type<V<A, B>> type() {
        var self = this;
        return Type.v(x -> self.apply(x).type());
    }

    public abstract Term<B> apply(Type<A> x);
}

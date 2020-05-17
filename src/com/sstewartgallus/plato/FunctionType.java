package com.sstewartgallus.plato;

public record FunctionType<A, B>(Type<A>domain, Type<B>range) implements CoreType<F<A, B>>, Type<F<A, B>> {
    @Override
    public <Y> Type<F<A, B>> unify(Type<Y> right) throws TypeCheckException {
        if (!(right instanceof FunctionType<?, ?> funType)) {
            throw new TypeCheckException(this, right);
        }
        return new FunctionType<>(domain.unify(funType.domain), range.unify(funType.range));
    }

    @Override
    public String toString() {
        return "(" + noBrackets() + ")";
    }

    private String noBrackets() {
        if (range instanceof FunctionType<?, ?> rangeF) {
            return domain + " → " + rangeF.noBrackets();
        }
        return domain + " → " + range;
    }


    public Class<?> erase() {
        return Term.class;
    }
}
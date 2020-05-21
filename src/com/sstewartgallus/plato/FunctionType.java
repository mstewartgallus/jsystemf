package com.sstewartgallus.plato;

import com.sstewartgallus.runtime.TypeDesc;

import java.util.Optional;

public record FunctionType<A, B>(Type<A>domain, Type<B>range) implements CoreType<F<A, B>>, Type<F<A, B>> {
    @Override
    public Optional<TypeDesc<F<A, B>>> describeConstable() {
        var dConst = domain.describeConstable();
        var rConst = range.describeConstable();

        if (dConst.isEmpty() || rConst.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(TypeDesc.ofFunction(dConst.get(), rConst.get()));
    }

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
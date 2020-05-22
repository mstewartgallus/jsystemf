package com.sstewartgallus.plato;

import java.util.Objects;
import java.util.Optional;

public record TypeApplyType<A, B>(Type<V<A, B>>f, Type<A>x) implements Type<B> {
    public TypeApplyType {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    @Override
    public Optional<TypeDesc<B>> describeConstable() {
        var fConst = f.describeConstable();
        var xConst = x.describeConstable();
        if (fConst.isEmpty() || xConst.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(TypeDesc.ofApply(fConst.get(), xConst.get()));
    }

    @Override
    public <Y> Type<B> unify(Type<Y> right) throws TypeCheckException {
        if (!(right instanceof TypeApplyType<?, ?> tApply)) {
            throw new TypeCheckException(this, right);
        }
        return new TypeApplyType<>(f.unify(tApply.f), x.unify(tApply.x));
    }

    @Override
    public Type<B> visitChildren(Term.Visitor visitor) {
        return new TypeApplyType<>(visitor.type(f), visitor.type(x));
    }

    // fixme...
    @Override
    public Class<?> erase() {
        return Term.class;
    }
}
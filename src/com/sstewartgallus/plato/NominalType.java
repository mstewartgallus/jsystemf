package com.sstewartgallus.plato;

import com.sstewartgallus.runtime.TypeDesc;

import java.util.Objects;
import java.util.Optional;

public final class NominalType<A> implements Type<A> {
    private final TypeTag<A> tag;

    // fixme... cache... in hashmap?
    private NominalType(TypeTag<A> tag) {
        this.tag = tag;
    }

    public static <A> NominalType<A> ofTag(TypeTag<A> tag) {
        return new NominalType<>(tag);
    }

    public TypeTag<A> tag() {
        return tag;
    }

    @Override
    public <Y> Type<A> unify(Type<Y> right) throws TypeCheckException {
        if (!(right instanceof NominalType<?> nominal)) {
            throw new TypeCheckException(this, right);
        }
        if (!Objects.equals(tag, nominal.tag)) {
            throw new TypeCheckException(this, right);
        }
        return this;
    }

    public Optional<TypeDesc<A>> describeConstable() {
        var tagC = tag.describeConstable();
        if (tagC.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(TypeDesc.ofNominal(tagC.get()));
    }
}
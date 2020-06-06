package com.sstewartgallus.plato.syntax.type;

import java.lang.constant.ConstantDesc;
import java.util.Optional;

public final class ThunkTag<A> implements TypeTag<A> {

    @Override
    public String toString() {
        return "U";
    }

    @Override
    public Optional<? extends ConstantDesc> describeConstable() {
        return Optional.of(TypeDesc.ofFunction());
    }

    @Override
    public Class<?> erase() {
        return Object.class;
    }
}
package com.sstewartgallus.plato.syntax.type;

import java.lang.constant.ConstantDesc;
import java.util.Optional;

public final class BoxTag<A> implements TypeTag<A> {

    @Override
    public String toString() {
        return "F";
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
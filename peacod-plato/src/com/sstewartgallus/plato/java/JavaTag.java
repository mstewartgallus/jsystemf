package com.sstewartgallus.plato.java;

import com.sstewartgallus.plato.syntax.type.TypeDesc;
import com.sstewartgallus.plato.syntax.type.TypeTag;

import java.lang.constant.ConstantDesc;
import java.util.Objects;
import java.util.Optional;

public record JavaTag<A>(Class<A>clazz) implements TypeTag<J<A>> {
    public JavaTag {
        Objects.requireNonNull(clazz);
    }

    @Override
    public Optional<ConstantDesc> describeConstable() {
        var cConst = clazz.describeConstable();

        if (cConst.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(TypeDesc.ofJavaClass(cConst.get()));
    }

    @Override
    public Class<?> erase() {
        return clazz;
    }

    @Override
    public boolean equals(Object value) {
        return value instanceof JavaTag<?> javaType && clazz == javaType.clazz;
    }

    @Override
    public String toString() {
        return clazz.getCanonicalName();
    }
}

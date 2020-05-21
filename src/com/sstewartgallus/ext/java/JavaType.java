package com.sstewartgallus.ext.java;

import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.TypeCheckException;
import com.sstewartgallus.runtime.TypeDesc;

import java.util.Objects;
import java.util.Optional;

public record JavaType<A>(Class<A>clazz) implements Type<J<A>> {
    public JavaType {
        Objects.requireNonNull(clazz);
    }

    @Override
    public Optional<TypeDesc<J<A>>> describeConstable() {
        var cConst = clazz.describeConstable();

        if (cConst.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of((TypeDesc) TypeDesc.ofJavaClass(cConst.get()));
    }

    public Class<?> erase() {
        return clazz;
    }

    public <Y> Type<J<A>> unify(Type<Y> right) throws TypeCheckException {
        if (!(right instanceof JavaType javaType)) {
            throw new TypeCheckException(this, right);
        }
        if (clazz != javaType.clazz) {
            throw new TypeCheckException(this, right);
        }
        return this;
    }

    @Override
    public String toString() {
        return clazz.getCanonicalName();
    }
}

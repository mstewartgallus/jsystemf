package com.sstewartgallus.ext.java;

import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.TypeCheckException;

import java.util.Objects;

public record JavaType<A>(Class<A>clazz) implements Type<J<A>> {

    public JavaType {
        Objects.requireNonNull(clazz);
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

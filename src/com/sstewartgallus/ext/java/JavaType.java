package com.sstewartgallus.ext.java;

import com.sstewartgallus.ext.variables.Id;
import com.sstewartgallus.ir.Signature;
import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.TypeCheckException;
import com.sstewartgallus.plato.V;

import java.util.Objects;

public record JavaType<A>(Class<A>clazz) implements Type<A> {

    public JavaType {
        Objects.requireNonNull(clazz);
    }

    public <Y> Type<A> unify(Type<Y> right) throws TypeCheckException {
        if (!(right instanceof JavaType<Y> javaType)) {
            throw new TypeCheckException(this, right);
        }
        if (clazz != javaType.clazz) {
            throw new TypeCheckException(this, right);
        }
        return this;
    }

    @Override
    public <Z> Signature<V<Z, A>> pointFree(Id<Z> argument) {
        return new Signature.K<>(new Signature.Pure<>(clazz));
    }

    @Override
    public String toString() {
        return clazz.getCanonicalName();
    }
}

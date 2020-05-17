package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.TypeCheckException;

import java.util.List;

public enum NilTupleType implements Type<N> {
    NIL;

    @Override
    public <Y> Type<N> unify(Type<Y> right) throws TypeCheckException {
        if (right == NIL) {
            return NIL;
        }
        throw new TypeCheckException(this, right);
    }

    public List<Class<?>> flatten() {
        return List.of();
    }
}
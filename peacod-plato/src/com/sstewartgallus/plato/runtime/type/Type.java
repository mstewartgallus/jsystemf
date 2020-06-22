package com.sstewartgallus.plato.runtime.type;

import com.sstewartgallus.plato.ir.type.TypeDesc;

import java.lang.constant.Constable;
import java.util.Optional;

public interface Type<X> extends Constable {
    default Optional<TypeDesc<X>> describeConstable() {
        throw new UnsupportedOperationException(getClass().toString());
    }
}
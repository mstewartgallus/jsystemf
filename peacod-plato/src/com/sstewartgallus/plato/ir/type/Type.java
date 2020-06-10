package com.sstewartgallus.plato.ir.type;

import java.lang.constant.Constable;
import java.util.Optional;

public interface Type<X> extends Constable {
    default Optional<TypeDesc<X>> describeConstable() {
        throw new UnsupportedOperationException(getClass().toString());
    }
}
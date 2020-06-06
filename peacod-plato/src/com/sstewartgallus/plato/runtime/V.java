package com.sstewartgallus.plato.runtime;

import com.sstewartgallus.plato.syntax.type.Type;

@SuppressWarnings("unused")
public interface V<A, B> extends U<V<A, B>> {
    B applyType(Type<A> type);

    default V<A, B> action() {
        return this;
    }
}

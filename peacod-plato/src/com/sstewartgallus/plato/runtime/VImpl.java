package com.sstewartgallus.plato.runtime;

import com.sstewartgallus.plato.ir.type.Type;

@SuppressWarnings("unused")
public interface VImpl<A, B> extends U<V<A, B>> {
    U<B> applyType(Type<A> type);
}

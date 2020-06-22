package com.sstewartgallus.plato.runtime;

import com.sstewartgallus.plato.runtime.type.Type;
import com.sstewartgallus.plato.runtime.type.U;

@SuppressWarnings("unused")
public abstract class VImpl<A, B> extends U<V<A, B>> {
    public abstract U<B> applyType(Type<A> type);
}

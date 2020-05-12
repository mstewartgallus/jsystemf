package com.sstewartgallus.ir;

import com.sstewartgallus.type.V;

public interface SigV<A, B> extends Signature<V<A, B>> {
    default Signature<B> range() {
        throw new UnsupportedOperationException(getClass().toString());
    }

    default Signature<B> apply(Signature<A> input) {
        throw new UnsupportedOperationException(getClass().toString());
    }
}

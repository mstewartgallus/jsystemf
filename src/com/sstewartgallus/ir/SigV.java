package com.sstewartgallus.ir;

import com.sstewartgallus.type.V;

interface SigV<A, B> extends Signature<V<A, B>> {
    default Signature<B> apply(Signature<A> input) {
        throw new UnsupportedOperationException(getClass().toString());
    }
}

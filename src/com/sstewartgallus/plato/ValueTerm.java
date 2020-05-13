package com.sstewartgallus.plato;

public interface ValueTerm<A> extends Term<A> {
    default A extract() {
        throw new UnsupportedOperationException(getClass().toString());
    }
}

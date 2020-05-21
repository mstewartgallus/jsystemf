package com.sstewartgallus.plato;

// fixme... make abstract base class?
public interface ValueTerm<A> extends Term<A> {
    default <B> Term<B> step(TermCont<A, B> k) {
        return k.apply(this);
    }

    default boolean reducible() {
        return false;
    }
}

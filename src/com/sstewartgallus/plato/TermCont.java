package com.sstewartgallus.plato;

public interface TermCont<A, B> {
    Term<B> apply(ValueTerm<A> value);
}

package com.sstewartgallus.plato;

public interface ThunkTerm<A> extends Term<A> {
    Term<A> stepThunk();
}

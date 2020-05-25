package com.sstewartgallus.plato;

import com.sstewartgallus.interpreter.Code;
import com.sstewartgallus.interpreter.PureCode;

/**
 * This is a convenience interface for terms that are always considered normalized
 */
public interface ValueTerm<A> extends Term<A> {

    @Override
    default Code<Term<A>> compile() {
        return new PureCode<>(this);
    }
}

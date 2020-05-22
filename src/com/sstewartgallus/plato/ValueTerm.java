package com.sstewartgallus.plato;

import com.sstewartgallus.interpreter.Effect;

/**
 * This is a convenience interface for terms that are always considered normalized
 */
public interface ValueTerm<A> extends Term<A> {

    @Override
    default Effect<Term<A>> interpret() {
        return Effect.pure(this);
    }

}

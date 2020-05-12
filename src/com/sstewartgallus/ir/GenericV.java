package com.sstewartgallus.ir;

import com.sstewartgallus.type.Type;
import com.sstewartgallus.type.V;

import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodHandles.Lookup;

/**
 * A category represents Term a -> Term b in a point free way
 * <p>
 * Generic represents Type a -> Term a in a point free way
 * <p>
 * Fixme: Look into a symbolic representation of my target https://www.youtube.com/watch?v=PwL2c6rO6co and then make a dsl for it.
 */
interface GenericV<A, B> extends Generic<V<A, B>> {

    default Bundle<?, ?, B> compileToHandle(MethodHandles.Lookup lookup, Type<A> klass) {
        throw new UnsupportedOperationException(getClass().toString());
    }

    default Generic<B> apply(Signature<A> x) {
        throw new UnsupportedOperationException(getClass().toString());
    }

    // fixme... get rid of this eventually...
    default Chunk<B> compile(Lookup lookup, Signature<A> klass) {
        return apply(klass).compile(lookup);
    }
}
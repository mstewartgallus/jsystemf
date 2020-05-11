package com.sstewartgallus.ir;

import com.sstewartgallus.mh.Arguments;
import com.sstewartgallus.type.F;

public interface Proof<A extends Arguments<A>, B, K> {
    record Trivial<A>() implements Proof<Arguments.None, A, A> {
    }

    record And<A, B, Tail extends Arguments<Tail>, Result>(
            Proof<Tail, Result, B>tail) implements Proof<Arguments.And<A, Tail>, Result, F<A, B>> {
    }
}

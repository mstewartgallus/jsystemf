package com.sstewartgallus.pass1;

import com.sstewartgallus.type.*;

public interface Args<A extends HList, B, R> {

    record Zero<A>() implements Args<Nil, A, A> {
    }

    record Add<A, B, L extends HList, R>(Type<A>argument, Args<L, B, R>tail) implements Args<Cons<A, L>, B, F<A, R>> {
    }
}

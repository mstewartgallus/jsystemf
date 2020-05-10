package com.sstewartgallus.pass1;

import com.sstewartgallus.type.*;

public interface Args<A extends HList<A>, B, R> {

    record Zero<A>() implements Args<HList.Nil, A, A> {
    }

    record Add<A, B, L extends HList<L>, R>(Type<A>argument, Args<L, B, R>tail) implements Args<HList.Cons<A, L>, B, F<A, R>> {
    }
}

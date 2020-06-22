package com.sstewartgallus.plato.runtime;

import com.sstewartgallus.plato.runtime.type.Stk;

public record PushStk<A, B>(A head, Stk<B>tail) implements Stk<Fn<A, B>> {
    @Override
    public String toString() {
        return head + " :: " + tail;
    }
}

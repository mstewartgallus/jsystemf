package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.runtime.Fn;
import com.sstewartgallus.plato.runtime.type.Stk;

public record PushValue<A, B>(Value<A>head, Value<Stk<B>>tail) implements Value<Stk<Fn<A, B>>> {
    @Override
    public String toString() {
        return head + " :: " + tail;
    }

    @Override
    public Value<Stk<Fn<A, B>>> visitChildren(ActionVisitor actionVisitor, ValueVisitor valueVisitor) {
        return new PushValue<>(valueVisitor.onValue(head), valueVisitor.onValue(tail));
    }
}
package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.runtime.Fn;
import com.sstewartgallus.plato.runtime.type.Stk;

import java.util.Objects;

public record PopAction<A, B, C>(Variable<A>head, Variable<Stk<B>>tail, Value<Stk<Fn<A, B>>>action,
                                 Action<C>body) implements Action<C> {
    public PopAction {
        Objects.requireNonNull(head);
        Objects.requireNonNull(action);
        Objects.requireNonNull(body);
    }

    @Override
    public String toString() {
        return action + " pop (" + head + ", " + tail + ") .\n" + body;
    }
}
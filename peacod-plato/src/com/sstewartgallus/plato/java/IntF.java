package com.sstewartgallus.plato.java;

import com.sstewartgallus.plato.runtime.Continuation;
import com.sstewartgallus.plato.runtime.type.Stk;
import com.sstewartgallus.plato.runtime.type.U;

// Fixme... not sure this makes sense...
public final class IntF extends U<IntF> {
    private final int value;

    private IntF(int value) {
        this.value = value;
    }

    public static IntF of(int value) {
        // fixme... cache ?
        return new IntF(value);
    }

    public int value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof IntF action && action.value == value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public <C> void enter(Continuation<C> context, Stk<IntF> action) {
        throw null;
    }
}
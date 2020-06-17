package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.runtime.U;

import java.util.concurrent.atomic.AtomicLong;

public record ThunkValue<A>(Action<A>action) implements Value<U<A>> {
    private static final AtomicLong IDS = new AtomicLong(0);

    @Override
    public String toString() {
        return "thunk " + action;
    }

}

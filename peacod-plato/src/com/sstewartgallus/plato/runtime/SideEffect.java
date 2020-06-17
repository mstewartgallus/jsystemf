package com.sstewartgallus.plato.runtime;

public final class SideEffect extends Throwable {
    // fixme.. use a clone methodhandle to avoid constructor not inlining costs...
    public SideEffect() {
        super(null, null, false, false);
    }
}
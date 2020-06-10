package com.sstewartgallus.plato.runtime;

abstract class Phantom {
    static {
        if (true)
            throw new Error("should never be touched");
    }

    protected Phantom() {
        throw new Error("never instance me");
    }
}

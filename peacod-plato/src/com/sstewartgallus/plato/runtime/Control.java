package com.sstewartgallus.plato.runtime;

public abstract class Control extends RuntimeException {
    Control() {
        super(null, null, false, false);
    }
}

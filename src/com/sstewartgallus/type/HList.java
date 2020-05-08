package com.sstewartgallus.type;

public abstract class HList {
    private HList(Void dummy) {
    }

    protected HList() {
        this(unreachable());
    }

    private static Void unreachable() {
        throw new UnsupportedOperationException("phantom type");
    }
}

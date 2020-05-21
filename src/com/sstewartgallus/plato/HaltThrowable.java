package com.sstewartgallus.plato;

public class HaltThrowable extends Throwable {
    public static final HaltThrowable HALT = new HaltThrowable();

    private HaltThrowable() {
        super(null, null, false, false);
    }
}

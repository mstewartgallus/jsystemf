package com.sstewartgallus.plato.java;

import com.sstewartgallus.plato.runtime.U;

// Fixme... not sure this makes sense...
public final class IntF extends Number implements U<IntF> {
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
    public int intValue() {
        return value;
    }

    @Override
    public long longValue() {
        return value;
    }

    @Override
    public float floatValue() {
        return value;
    }

    @Override
    public double doubleValue() {
        return value;
    }
}
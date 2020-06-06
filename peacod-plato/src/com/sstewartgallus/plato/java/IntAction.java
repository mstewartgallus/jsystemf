package com.sstewartgallus.plato.java;

import com.sstewartgallus.plato.runtime.F;

public record IntAction(int value) implements F<Integer> {
    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public Integer evaluate() {
        return value;
    }
}
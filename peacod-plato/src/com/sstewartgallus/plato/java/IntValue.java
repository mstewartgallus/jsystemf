package com.sstewartgallus.plato.java;

import com.sstewartgallus.plato.ir.cps.Value;

public record IntValue(int value) implements Value<Integer> {
    @Override
    public String toString() {
        return String.valueOf(value);
    }

}

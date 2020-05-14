package com.sstewartgallus.primitives;

import com.sstewartgallus.ext.AddThunk;
import com.sstewartgallus.ext.java.IntValue;
import com.sstewartgallus.plato.Term;

public final class Prims {

    private Prims() {
    }

    public static Term<Integer> of(int value) {
        return new IntValue(value);
    }

    public static Term<Integer> add(Term<Integer> left, Term<Integer> right) {
        return new AddThunk(left, right);
    }

}


package com.sstewartgallus.primitives;

import com.sstewartgallus.ext.AddThunk;
import com.sstewartgallus.ext.java.IntValue;
import com.sstewartgallus.ext.java.J;
import com.sstewartgallus.plato.Term;

public final class Prims {

    private Prims() {
    }

    public static Term<J<Integer>> of(int value) {
        return new IntValue(value);
    }

    public static Term<J<Integer>> add(Term<J<Integer>> left, Term<J<Integer>> right) {
        return new AddThunk(left, right);
    }

}


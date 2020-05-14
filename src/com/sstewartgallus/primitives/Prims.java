package com.sstewartgallus.primitives;

import com.sstewartgallus.ext.AddThunk;
import com.sstewartgallus.ext.java.PureValue;
import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.Type;

public final class Prims {

    private Prims() {
    }

    public static Term<Integer> of(int value) {
        return new PureValue<>(Type.INT, value);
    }

    public static Term<Integer> add(Term<Integer> left, Term<Integer> right) {
        return new AddThunk(left, right);
    }

}


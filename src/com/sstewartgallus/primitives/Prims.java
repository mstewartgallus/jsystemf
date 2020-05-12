package com.sstewartgallus.primitives;

import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.Type;

public final class Prims {

    private Prims() {
    }

    public static Term<Integer> of(int value) {
        return Term.pure(Type.INT, value);
    }

    public static Term<Integer> add(Term<Integer> left, Term<Integer> right) {
        throw new UnsupportedOperationException("unimplemented");
    }

    public static Term<Boolean> lessThan(Term<Integer> left, Term<Integer> right) {
        throw new UnsupportedOperationException("unimplemented");
    }

}


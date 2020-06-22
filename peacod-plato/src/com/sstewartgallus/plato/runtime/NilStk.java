package com.sstewartgallus.plato.runtime;

import com.sstewartgallus.plato.runtime.type.Behaviour;
import com.sstewartgallus.plato.runtime.type.Stk;

public final class NilStk implements Stk<Behaviour> {
    public static final NilStk NIL = new NilStk();

    private NilStk() {
    }

    @Override
    public String toString() {
        return "NIL";
    }
}

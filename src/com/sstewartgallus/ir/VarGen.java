package com.sstewartgallus.ir;

import com.sstewartgallus.term.Var;
import com.sstewartgallus.type.Type;

public final class VarGen {
    public <A> Var<A> createArgument(Type<A> type) {
        return new Var<>(type, argNumber++);
    }

    private int argNumber = 0;
}
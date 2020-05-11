package com.sstewartgallus.ir;

import com.sstewartgallus.type.Var;

public final class VarGen {
    private int argNumber = 0;

    public <A> Var<A> createArgument() {
        return new Var<>(argNumber++);
    }
}
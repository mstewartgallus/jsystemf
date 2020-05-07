package com.sstewartgallus.ir;

import com.sstewartgallus.type.Type;

public final class TVarGen {
    private int argNumber = 0;

    public <A> Type.Var<A> createTypeVar() {
        return new Type.Var<>(argNumber++);
    }
}
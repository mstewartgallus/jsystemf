package com.sstewartgallus.ir;

import com.sstewartgallus.type.TVar;

public final class TVarGen {
    private int argNumber = 0;

    public <A> TVar<A> createTPass0Var() {
        return new TVar<>(argNumber++);
    }
}
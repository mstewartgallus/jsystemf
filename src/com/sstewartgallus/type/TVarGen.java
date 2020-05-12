package com.sstewartgallus.type;

public final class TVarGen {
    private int argNumber = 0;

    public <A> TVar<A> createTypeVar() {
        return new TVar<>(argNumber++);
    }
}
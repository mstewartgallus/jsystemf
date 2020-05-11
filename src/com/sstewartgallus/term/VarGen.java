package com.sstewartgallus.term;

public final class VarGen {
    private int argNumber = 0;

    public <A> Var<A> createArgument() {
        return new Var<>(argNumber++);
    }
}
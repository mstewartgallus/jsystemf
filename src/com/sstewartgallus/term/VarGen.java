package com.sstewartgallus.term;

public final class VarGen {
    private int argNumber = 0;

    public <A> Id<A> createId() {
        return new Id<>(argNumber++);
    }
}
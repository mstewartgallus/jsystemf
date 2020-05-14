package com.sstewartgallus.ext.variables;

public final class IdGen {
    private int argNumber = 0;

    public <A> Id<A> createId() {
        return new Id<>(argNumber++);
    }
}
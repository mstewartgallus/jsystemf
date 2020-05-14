package com.sstewartgallus.ext.variables;

public final class IdGen {
    public <A> Id<A> createId() {
        return new Id<>();
    }
}
package com.sstewartgallus.interpreter;

public final class Id<A> {
    public A value;

    @Override
    public String toString() {
        return "Id@" + hashCode();
    }
}

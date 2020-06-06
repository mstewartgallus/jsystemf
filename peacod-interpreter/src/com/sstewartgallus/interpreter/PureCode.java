package com.sstewartgallus.interpreter;

public record PureCode<A>(A value) implements Code<A> {
    @Override
    public String toString() {
        return value.toString();
    }

}

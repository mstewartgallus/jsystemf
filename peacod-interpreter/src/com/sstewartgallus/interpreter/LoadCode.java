package com.sstewartgallus.interpreter;

import java.util.Objects;

public final class LoadCode<A> implements Code<A> {
    public final Id<A> variable;

    public LoadCode(Id<A> id) {
        variable = id;
    }

    @Override
    public String toString() {
        return Objects.toString(variable);
    }

}

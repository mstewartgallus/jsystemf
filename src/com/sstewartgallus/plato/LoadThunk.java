package com.sstewartgallus.plato;

import java.util.Objects;

// fixme... should be a nonpure extension to the term language...
// fixme... is it normal?
public record LoadThunk<A>(Type<A>type, Id<A>variable) implements ThunkTerm<A> {
    public LoadThunk {
        Objects.requireNonNull(variable);
    }

    @Override
    public String toString() {
        return "v" + variable.toString();
    }

    @Override
    public <X> X visit(Visitor<X, A> visitor) {
        return visitor.onLoad(type, variable);
    }
}

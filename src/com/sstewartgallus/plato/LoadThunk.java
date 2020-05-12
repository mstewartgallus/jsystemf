package com.sstewartgallus.plato;

import java.util.Objects;

// fixme... should be a nonpure extension to the term language...
// fixme... is it a thunk or a value?
public record LoadThunk<A>(Type<A>type, Id<A>variable) implements ExtensionThunk<A> {
    public LoadThunk {
        Objects.requireNonNull(type);
        Objects.requireNonNull(variable);
    }

    @Override
    public String toString() {
        return "v" + variable.toString();
    }

    @Override
    public Term<A> stepThunk() {
        throw new UnsupportedOperationException("unimplemented");
    }
}

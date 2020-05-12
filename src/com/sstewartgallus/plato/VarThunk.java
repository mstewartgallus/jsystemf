package com.sstewartgallus.plato;

import java.util.Objects;

// fixme... should be a nonpure extension to the term language...
// fixme... is it a thunk or a value?

/**
 * NOT a core term of the language...
 *
 * @param <A>
 */
public record VarThunk<A>(Type<A>type, Id<A>variable) implements ThunkTerm<A>, CoreTerm<A> {
    public VarThunk {
        Objects.requireNonNull(type);
        Objects.requireNonNull(variable);
    }

    @Override
    public String toString() {
        return "v" + variable;
    }

    @Override
    public Term<A> stepThunk() {
        throw new UnsupportedOperationException("unimplemented");
    }

    public <X> Term<A> substitute(Id<X> argument, Term<X> replacement) {
        if (variable.equals(argument)) {
            return (Term) replacement;
        }
        return this;
    }
}

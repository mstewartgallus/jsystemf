package com.sstewartgallus.plato;

import java.util.Objects;

// fixme... should be a nonpure extension to the term language...
// fixme... is it a thunk or a value?

/**
 * NOT a core term of the language...
 *
 * @param <A>
 */
public record VarValue<A>(Type<A>type, Id<A>variable) implements ValueTerm<A>, CoreTerm<A>, Comparable<VarValue<?>> {
    public VarValue {
        Objects.requireNonNull(type);
        Objects.requireNonNull(variable);
    }

    @Override
    public String toString() {
        return "v" + variable;
    }

    public <X> Term<A> substitute(Id<X> argument, Term<X> replacement) {
        if (variable.equals(argument)) {
            return (Term) replacement;
        }
        return this;
    }

    @Override
    public int compareTo(VarValue<?> o) {
        return variable.compareTo(o.variable);
    }
}

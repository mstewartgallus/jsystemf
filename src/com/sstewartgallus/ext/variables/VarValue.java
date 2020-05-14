package com.sstewartgallus.ext.variables;

import com.sstewartgallus.plato.CoreTerm;
import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.ValueTerm;

import java.util.Objects;

// fixme... should be a nonpure extension to the list language...
// fixme... is it a thunk or a value?

/**
 * NOT a core list of the language...
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

    @Override
    public <X> Term<A> substitute(Id<X> v, Type<X> replacement) {
        return new VarValue<>(type.substitute(v, replacement), variable);
    }

    @Override
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

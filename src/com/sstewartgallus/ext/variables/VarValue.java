package com.sstewartgallus.ext.variables;

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
public record VarValue<A>(Type<A>type, Id<A>variable) implements ValueTerm<A>, Comparable<VarValue<?>> {
    public VarValue {
        Objects.requireNonNull(type);
        Objects.requireNonNull(variable);
    }

    @Override
    public String toString() {
        return "v" + variable;
    }

    public <X> Term<X> substituteIn(Term<X> root, Term<A> replacement) {
        return root.visit(new Visitor() {
            @Override
            public <T> Term<T> term(Term<T> term) {
                if (!(term instanceof VarValue<T> varValue)) {
                    return term.visitChildren(this);
                }

                if (varValue.variable == variable) {
                    return (Term) replacement;
                }
                return varValue;
            }
        });
    }

    @Override
    public int compareTo(VarValue<?> o) {
        return variable.compareTo(o.variable);
    }
}

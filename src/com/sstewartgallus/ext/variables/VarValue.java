package com.sstewartgallus.ext.variables;

import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.TypeCheckException;
import com.sstewartgallus.plato.ValueTerm;

import java.util.Objects;

// fixme... should be a nonpure extension to the list language...
// fixme... is it a thunk or a value?

/**
 * NOT a core list of the language...
 *
 * @param <A>
 */
public final class VarValue<A> implements ValueTerm<A>, Comparable<VarValue<?>> {
    private final Type<A> type;
    private final Id<A> variable;

    public VarValue(Type<A> type) {
        this(type, new Id<>());
    }

    private VarValue(Type<A> type, Id<A> variable) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(variable);
        this.type = type;
        this.variable = variable;
    }

    @Override
    public Term<A> visitChildren(Visitor visitor) {
        return new VarValue<>(visitor.type(type), variable);
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

    @Override
    public Type<A> type() throws TypeCheckException {
        return type;
    }

    public Id<A> variable() {
        return variable;
    }
}

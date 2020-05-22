package com.sstewartgallus.ext.variables;

import com.sstewartgallus.plato.NominalTerm;
import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.TermTag;
import com.sstewartgallus.plato.Type;

import java.lang.constant.ConstantDesc;
import java.util.Objects;
import java.util.Optional;

// fixme... should be a nonpure extension to the list language ?
public final class VarValue<A> implements TermTag<A>, Comparable<VarValue<?>> {
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

    public Type<A> type() {
        return type;
    }

    @Override
    public String toString() {
        return "v" + variable;
    }

    public <X> Term<X> substituteIn(Term<X> root, Term<A> replacement) {
        return root.visit(new Term.Visitor() {
            @Override
            public <T> Term<T> term(Term<T> term) {
                if (!(term instanceof NominalTerm<T> nominalTerm && nominalTerm.tag() instanceof VarValue<T> varValue)) {
                    return term.visitChildren(this);
                }

                if (varValue.variable == variable) {
                    return (Term) replacement;
                }
                return nominalTerm;
            }
        });
    }

    @Override
    public int compareTo(VarValue<?> o) {
        return variable.compareTo(o.variable);
    }

    @Override
    public Optional<? extends ConstantDesc> describeConstable() {
        return Optional.empty();
    }
}

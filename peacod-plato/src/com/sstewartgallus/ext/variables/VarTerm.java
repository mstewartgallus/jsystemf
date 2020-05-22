package com.sstewartgallus.ext.variables;

import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.ValueTerm;
import com.sstewartgallus.plato.TermDesc;

import java.util.Objects;
import java.util.Optional;

// fixme... should be a nonpure extension to the list language ?
public final class VarTerm<A> implements ValueTerm<A>, Comparable<VarTerm<?>> {
    private final Type<A> type;
    private final Id<A> variable;

    public VarTerm(Type<A> type) {
        this(type, new Id<>());
    }

    private VarTerm(Type<A> type, Id<A> variable) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(variable);
        this.type = type;
        this.variable = variable;
    }

    public Type<A> type() {
        return type;
    }

    @Override
    public Term<A> visitChildren(Visitor visitor) {
        return new VarTerm<>(visitor.type(type), variable);
    }

    @Override
    public String toString() {
        return "v" + variable;
    }


    public <X> Term<X> substituteIn(Term<X> root, Term<A> replacement) {
        return root.visit(new Term.Visitor() {
            @Override
            public <T> Term<T> term(Term<T> term) {
                if (!(term instanceof VarTerm<T> varValue)) {
                    return term.visitChildren(this);
                }

                if (varValue.variable == variable) {
                    return (Term) replacement;
                }
                return term;
            }
        });
    }

    @Override
    public int compareTo(VarTerm<?> o) {
        return variable.compareTo(o.variable);
    }

    @Override
    public Optional<TermDesc<A>> describeConstable() {
        return Optional.empty();
    }
}

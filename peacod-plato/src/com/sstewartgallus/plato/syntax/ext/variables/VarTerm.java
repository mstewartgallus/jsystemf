package com.sstewartgallus.plato.syntax.ext.variables;

import com.sstewartgallus.plato.cbpv.Code;
import com.sstewartgallus.plato.cbpv.ForceCode;
import com.sstewartgallus.plato.syntax.term.Environment;
import com.sstewartgallus.plato.syntax.term.Solution;
import com.sstewartgallus.plato.syntax.term.Term;
import com.sstewartgallus.plato.syntax.type.Type;

import java.util.Objects;

// fixme... should be a nonpure extension to the list language ?
public final class VarTerm<A> implements Term<A>, Comparable<VarTerm<?>> {
    public final Id<A> variable;
    private final Type<A> type;

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
    public Term<A> resolve(Solution solution) {
        return new VarTerm<>(type.resolve(solution), variable);
    }

    @Override
    public Term<A> visitChildren(Visitor visitor) {
        return new VarTerm<>(visitor.type(type), variable);
    }

    @Override
    public String toString() {
        return "v" + variable;
    }

    // fixme... not sure of use...
    public <X> Term<X> substituteIn(Term<X> root, Term<A> replacement) {
        return root.visit(new Term.Visitor() {
            @Override
            public <T> Term<T> term(Term<T> term) {
                if (!(term instanceof VarTerm<?> varValue)) {
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
    public Code<A> compile(Environment environment) {
        return new ForceCode<>(environment.get(this));
    }
}

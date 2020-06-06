package com.sstewartgallus.plato.syntax.type;

import com.sstewartgallus.plato.runtime.U;
import com.sstewartgallus.plato.runtime.V;
import com.sstewartgallus.plato.syntax.ext.variables.VarType;
import com.sstewartgallus.plato.syntax.term.Solution;
import com.sstewartgallus.plato.syntax.term.Term;
import org.projog.core.term.Structure;
import org.projog.core.term.Variable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public record TypeApplyType<A, B>(Type<V<A, B>>f, Type<A>x) implements Type<B> {
    public TypeApplyType {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    @Override
    public Optional<TypeDesc<B>> describeConstable() {
        var fConst = f.describeConstable();
        var xConst = x.describeConstable();
        if (fConst.isEmpty() || xConst.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(TypeDesc.ofApply(fConst.get(), xConst.get()));
    }

    @Override
    public void holes(Set<VarType<?>> holes) {
        f.holes(holes);
        x.holes(holes);
    }

    @Override
    public org.projog.core.term.Term toTerm(Map<VarType<?>, Variable> holevars) {
        return Structure.createStructure("apply", new org.projog.core.term.Term[]{f.toTerm(holevars), x.toTerm(holevars)});
    }

    @Override
    public Type<B> visitChildren(Term.Visitor visitor) {
        return new TypeApplyType<>(visitor.type(f), visitor.type(x));
    }

    // fixme...
    @Override
    public Class<?> erase() {
        return U.class;
    }

    @Override
    public Type<B> resolve(Solution environment) {
        return new TypeApplyType<>(f.resolve(environment), x.resolve(environment));
    }

    @Override
    public String toString() {
        return "(" + f + " " + x + ")";
    }
}
package com.sstewartgallus.plato.syntax.type;

import com.sstewartgallus.plato.syntax.ext.variables.VarType;
import com.sstewartgallus.plato.syntax.term.Solution;
import org.projog.core.term.Atom;
import org.projog.core.term.Term;
import org.projog.core.term.Variable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class NominalType<A> implements Type<A> {
    private final TypeTag<A> tag;

    // fixme... cache... in hashmap?
    public NominalType(TypeTag<A> tag) {
        this.tag = tag;
    }

    public static <A> NominalType<A> ofTag(TypeTag<A> tag) {
        return new NominalType<>(tag);
    }

    public TypeTag<A> tag() {
        return tag;
    }

    @Override
    public Optional<TypeDesc<A>> describeConstable() {
        var tagC = tag.describeConstable();
        if (tagC.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(TypeDesc.ofNominal(tagC.get()));
    }

    @Override
    public Type<A> resolve(Solution solution) {
        if (tag instanceof VarType<A> varType) {
            var result = solution.get(varType);
            if (result == null) {
                System.err.println("no solution found for type " + varType + " in " + solution);

                return this;
//                throw new RuntimeException("no solution found for type " + varType + " in " + this);
            }
            return result;
        }
        return this;
    }

    @Override
    public Term toTerm(Map<VarType<?>, Variable> holevars) {
        if (tag instanceof VarType) {
            return holevars.get(tag);
        }
        return new Atom(tag.toString());
    }

    @Override
    public void holes(Set<VarType<?>> holes) {
        if (tag instanceof VarType<A> varType) {
            holes.add(varType);
        }
    }

    @Override
    public Class<?> erase() {
        return tag.erase();
    }

    public String toString() {
        return tag.toString();
    }
}
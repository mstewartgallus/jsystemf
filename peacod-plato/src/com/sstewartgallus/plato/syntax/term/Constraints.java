package com.sstewartgallus.plato.syntax.term;

import com.sstewartgallus.plato.syntax.ext.variables.VarType;
import com.sstewartgallus.plato.syntax.type.NominalType;
import com.sstewartgallus.plato.syntax.type.Type;
import org.projog.api.Projog;

import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

public final class Constraints {
    private final Set<Constraint<?>> constraints;

    public Constraints() {
        this.constraints = Set.of();
    }

    private Constraints(Set<Constraint<?>> constraints) {
        this.constraints = constraints;
    }

    public static Constraints unify(Constraints... constraints) {
        var set = new HashSet<Constraint<?>>();
        for (var entry : constraints) {
            set.addAll(entry.constraints);
        }
        return new Constraints(set);
    }

    public Solution solve() {
        return Solution.solve(constraints);
    }

    public <A> Constraints constrainEqual(Type<A> left, Type<A> right) {
        if (left.equals(right)) {
            return this;
        }
        var copy = new HashSet<>(constraints);
        copy.add(Constraint.of(left, right));
        return new Constraints(copy);
    }

    @Override
    public String toString() {
        return constraints.toString();
    }
}

record Constraint<A>(Type<A>left, Type<A>right) {
    public static <A> Constraint<A> of(Type<A> left, Type<A> right) {
        if (left.hashCode() < right.hashCode() || right instanceof NominalType nom && nom.tag() instanceof VarType) {
            var tmp = right;
            right = left;
            left = tmp;
        }
        return new Constraint<>(left, right);
    }

    @Override
    public String toString() {
        return left + "~" + right;
    }
}
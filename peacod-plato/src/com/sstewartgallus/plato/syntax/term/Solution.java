package com.sstewartgallus.plato.syntax.term;

import com.sstewartgallus.plato.java.IntType;
import com.sstewartgallus.plato.syntax.ext.variables.VarType;
import com.sstewartgallus.plato.syntax.type.NominalType;
import com.sstewartgallus.plato.syntax.type.Type;
import com.sstewartgallus.plato.syntax.type.TypeApplyType;
import org.projog.api.Projog;
import org.projog.core.ProjogDefaultProperties;
import org.projog.core.term.Atom;
import org.projog.core.term.ListFactory;
import org.projog.core.term.Structure;
import org.projog.core.term.Variable;

import java.io.InputStreamReader;
import java.util.*;

public final class Solution {
    private final Map<VarType<?>, Type<?>> solutions;

    private Solution(Map<VarType<?>, Type<?>> solutions) {
        this.solutions = solutions;
    }

    // fixme.. look into JProlog or similar..
    private static <A> void processEntry(List<Constraint<?>> allConstraints, Constraint<A> entry, Map<VarType<?>, Type<?>> map) {
        var left = entry.left();
        var right = entry.right();

        if (left.equals(right)) {
            return;
        }

        // fixme... turn into methods?
        if (left instanceof TypeApplyType<?, A> leftAp
                && right instanceof TypeApplyType<?, A> rightAp) {
            allConstraints.add(Constraint.of((Type) leftAp.f(), rightAp.f()));
            allConstraints.add(Constraint.of((Type) leftAp.x(), rightAp.x()));
            return;
        }

        if (right instanceof NominalType<A> rightN && rightN.tag() instanceof VarType) {
            var tmp = right;
            right = left;
            left = tmp;
        }

        if (left instanceof NominalType<A> leftN && leftN.tag() instanceof VarType<A> leftVarType) {
            map.put(leftVarType, right);
            return;
        }

        throw new UnsupportedOperationException(left.toString() + " ~ " + right.toString());
    }

    static <A> Solution solve(Set<Constraint<?>> constraints) {
        var holes = new HashSet<VarType<?>>();
        for (var constraint : constraints) {
            constraint.left().holes(holes);
            constraint.right().holes(holes);
        }

        var prolog = new Projog(new ProjogDefaultProperties() {
            @Override
            public boolean isRuntimeCompilationEnabled() {
                return false;
            }
        });

        var theSource = Solution.class.getResourceAsStream("com/sstewartgallus/plato/frontend/typeinference.pl");

        prolog.consultReader(new InputStreamReader(theSource));

        Map<VarType<?>, Variable> holevars = new HashMap<>();
        List<org.projog.core.term.Term> holeslist = new ArrayList<>();
        for (var hole : holes) {
            var holename = hole.toString().toLowerCase();
            var variable = new Variable(holename.toUpperCase());
            holevars.put(hole, variable);

            var entry = Structure.createStructure("-", new org.projog.core.term.Term[]{new Atom(holename), variable});
            holeslist.add(entry);
        }

        List<org.projog.core.term.Term> datalist = new ArrayList<>();
        for (var constraint : constraints) {
            var entry = Structure.createStructure("=", new org.projog.core.term.Term[]{
                    constraint.left().toTerm(holevars),
                    constraint.right().toTerm(holevars)});
            datalist.add(entry);
        }

        var assertQuery = prolog.query("assert(data(Holes, Data)).");
        {
            var result = assertQuery.getResult();
            result.setTerm("Holes", ListFactory.createList(holeslist));
            result.setTerm("Data", ListFactory.createList(datalist));
            while (result.next()) ;
        }

        var query = prolog.query("hole_value(Key, Value).");

        var map = new HashMap<VarType<?>, Type<?>>();
        for (var hole : holes) {
            var result = query.getResult();
            result.setTerm("Key", new Atom(hole.toString().toLowerCase()));
            if (result.isExhausted()) {
                throw new Error("no solution for hole " + hole);
            }
            while (result.next()) {
                var valueterm = result.getTerm("Value");
                map.put(hole, toTerm(valueterm));
            }
        }
        return new Solution(map);
    }

    private static Type<?> toTerm(org.projog.core.term.Term value) {
        if (value.getType().isStructure()) {
            if (!"apply".equals(value.getName())) {
                throw null;
            }
            return new TypeApplyType(toTerm(value.getArgument(0)), toTerm(value.getArgument(1)));
        }
        return switch (value.getName()) {
            case "(->)" -> Type.function();
            case "F" -> Type.returnType();
            case "U" -> Type.thunkType();
            case "int" -> IntType.INT_TYPE;
            default -> throw null;
        };
    }

    @Override
    public String toString() {
        return solutions.toString();
    }

    public <A> Type<A> get(VarType<A> varType) {
        var result = solutions.get(varType);
        return (Type<A>) result;
    }
}

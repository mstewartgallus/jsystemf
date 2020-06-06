package com.sstewartgallus.plato.java;

import com.sstewartgallus.plato.cbpv.Code;
import com.sstewartgallus.plato.cbpv.LetToCode;
import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.syntax.ext.variables.VarTerm;
import com.sstewartgallus.plato.syntax.term.*;
import com.sstewartgallus.plato.syntax.type.Type;
import com.sstewartgallus.plato.syntax.type.TypeCheckException;

// fixme... make nominal term or something...
public record AddTerm(Term<F<Integer>>left, Term<F<Integer>>right) implements Term<F<Integer>> {
    @Override
    public String toString() {
        return "(+ " + left + " " + right + ")";
    }

    @Override
    public Type<F<Integer>> type() {
        return IntType.INT_TYPE.unboxed();
    }

    @Override
    public Term<F<Integer>> visitChildren(Visitor visitor) {
        return this;
    }

    @Override
    public Code<F<Integer>> compile(Environment environment) {
        var leftC = left.compile(environment);
        var rightC = right.compile(environment);
        return LetToCode.of(IntType.INT_TYPE, leftC, l ->
                LetToCode.of(IntType.INT_TYPE, rightC, r ->
                        new AddCode(l, r)));
    }

    @Override
    public Constraints findConstraints() throws TypeCheckException {
        return Constraints.unify(left.findConstraints(), right.findConstraints())
                .constrainEqual(left.type(), IntType.INT_TYPE.unboxed())
                .constrainEqual(right.type(), IntType.INT_TYPE.unboxed());
    }

    @Override
    public Term<F<Integer>> resolve(Solution solution) {
        return new AddTerm(left.resolve(solution), right.resolve(solution));
    }

}

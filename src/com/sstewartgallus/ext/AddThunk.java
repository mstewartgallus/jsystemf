package com.sstewartgallus.ext;

import com.sstewartgallus.ext.java.J;
import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.ThunkTerm;
import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.TypeCheckException;

import java.util.Objects;

// fixme... don't use this...
public record AddThunk(Term<J<Integer>>left, Term<J<Integer>>right) implements ThunkTerm<J<Integer>> {
    public AddThunk {
        Objects.requireNonNull(left);
        Objects.requireNonNull(right);
    }

    @Override
    public Term<J<Integer>> visitChildren(Visitor visitor) {
        return new AddThunk(visitor.term(left), visitor.term(right));
    }

    @Override
    public Type<J<Integer>> type() throws TypeCheckException {
        return Type.INT;
    }

    @Override
    public String toString() {
        return "(+ " + left + " " + right + ")";
    }

    @Override
    public Term<J<Integer>> stepThunk() {
        throw null;
        // return Prims.of(Interpreter.normalize(left).extract() + Interpreter.normalize(right).extract());
    }
}

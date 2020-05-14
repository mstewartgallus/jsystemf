package com.sstewartgallus.ext;

import com.sstewartgallus.ext.variables.Id;
import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.ThunkTerm;
import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.TypeCheckException;

import java.util.Objects;

// fixme... don't use this...
public record AddThunk(Term<Integer>left, Term<Integer>right) implements ThunkTerm<Integer> {
    public AddThunk {
        Objects.requireNonNull(left);
        Objects.requireNonNull(right);
    }

    @Override
    public Type<Integer> type() throws TypeCheckException {
        return Type.INT;
    }

    @Override
    public String toString() {
        return "(+ " + left + " " + right + ")";
    }

    @Override
    public <X> Term<Integer> substitute(Id<X> v, Term<X> replacement) {
        return new AddThunk(left.substitute(v, replacement), right.substitute(v, replacement));
    }

    @Override
    public Term<Integer> stepThunk() {
        throw null;
        // return Prims.of(Interpreter.normalize(left).extract() + Interpreter.normalize(right).extract());
    }
}

package com.sstewartgallus.extensions;

import com.sstewartgallus.plato.*;
import com.sstewartgallus.primitives.Prims;

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
        return Prims.of(Interpreter.normalize(left).extract() + Interpreter.normalize(right).extract());
    }
}

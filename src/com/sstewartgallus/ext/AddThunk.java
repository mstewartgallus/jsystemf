package com.sstewartgallus.ext;

import com.sstewartgallus.ext.java.J;
import com.sstewartgallus.plato.*;

import java.util.Objects;
import java.util.function.Function;

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
    public <B> Term<B> stepThunk(Function<ValueTerm<J<Integer>>, Term<B>> k) {
        throw null;
    }

    @Override
    public Type<J<Integer>> type() throws TypeCheckException {
        return Type.INT;
    }

    @Override
    public String toString() {
        return "(+ " + left + " " + right + ")";
    }
}

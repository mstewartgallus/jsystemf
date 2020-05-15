package com.sstewartgallus.ext.pointfree;

import com.sstewartgallus.ext.tuples.Nil;
import com.sstewartgallus.ext.tuples.NilValue;
import com.sstewartgallus.plato.*;

public record Pick<B>(Term<F<Nil, B>>k) implements ThunkTerm<B> {
    @Override
    public Term<B> stepThunk() {
        var kNorm = (LambdaValue<Nil, B>) Interpreter.normalize(k);
        return kNorm.apply(NilValue.NIL);
    }

    @Override
    public Type<B> type() throws TypeCheckException {
        var kType = (FunctionType<Nil, B>) k.type();
        return kType.range();
    }

    @Override
    public String toString() {
        return "(eval " + k + ")";
    }

    @Override
    public Term<B> visitChildren(Visitor visitor) {
        return new Pick<>(visitor.term(k));
    }
}

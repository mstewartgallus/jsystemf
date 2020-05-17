package com.sstewartgallus.ext.mh;

import com.sstewartgallus.ext.tuples.Signature;
import com.sstewartgallus.ext.tuples.Tuple;
import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.ThunkTerm;
import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.TypeCheckException;

import java.lang.invoke.MethodHandle;

// fixme... could be an abstract class I suppose or another pass could lower to that...
public record MethodHandleThunk<A extends Tuple<A>, B, C>(Signature<A, B, C>sig,
                                                          MethodHandle methodHandle) implements ThunkTerm<C> {
    @Override
    public Term<C> stepThunk() {
        return sig.stepThunk(methodHandle);
    }

    @Override
    public Type<C> type() throws TypeCheckException {
        return sig.type();
    }

    @Override
    public Term<C> visitChildren(Visitor visitor) {
        return this;
    }

    public String toString() {
        return methodHandle.toString();
    }

}

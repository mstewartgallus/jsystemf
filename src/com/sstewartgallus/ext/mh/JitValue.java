package com.sstewartgallus.ext.mh;

import com.sstewartgallus.ext.tuples.Signature;
import com.sstewartgallus.ext.tuples.Tuple;
import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.TypeCheckException;
import com.sstewartgallus.plato.ValueTerm;

import java.lang.invoke.MethodHandle;

// fixme... could be an abstract class I suppose or another pass could lower to that...
// fixme... establish an invariant that this must always be a function or a forall.
public record JitValue<A extends Tuple<A>, B, C>(Signature<A, B, C>sig,
                                                 MethodHandle methodHandle) implements ValueTerm<C> {
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

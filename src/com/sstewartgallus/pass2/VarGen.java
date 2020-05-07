package com.sstewartgallus.pass2;

import com.sstewartgallus.term.Term;
import com.sstewartgallus.type.Type;

public final class VarGen {
    public <A> Term.Var<A> createArgument(Type<A> type) {
        return new Term.Var<>(type, argNumber++);
    }

    private int argNumber = 0;

    public <A> Type.Var<A> createTypeVar() {
        return new Type.Var<>(argNumber++);
    }
}
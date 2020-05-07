package com.sstewartgallus.ir;

import com.sstewartgallus.term.Term;
import com.sstewartgallus.type.Type;

public final class VarGen {
    public <A> Term.Var<A> createArgument(Type<A> type) {
        return new Term.Var<>(type, argNumber++);
    }

    private int argNumber = 0;
}
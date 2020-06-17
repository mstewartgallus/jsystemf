package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.Jit;
import com.sstewartgallus.plato.runtime.U;

public record ForceCode<A>(Literal<U<A>>thunk) implements Code<A> {
    public static <A> Code<A> of(Literal<U<A>> lit) {
        if (lit instanceof ThunkLiteral<A> thunk) {
            return thunk.code();
        }
        return new ForceCode<>(lit);
    }

    @Override
    public void compile(Jit.Environment environment) {
        thunk.compile(environment);
        System.err.println("fixme... force thunk");
    }

    @Override
    public TypeDesc<A> type() {
        var fType = (TypeDesc.TypeApplicationDesc<A, U<A>>) thunk.type();
        return fType.x();
    }

    @Override
    public String toString() {
        return "! " + thunk;
    }
}
package com.sstewartgallus.plato.cbpv;

import com.sstewartgallus.plato.runtime.U;
import com.sstewartgallus.plato.syntax.type.Type;

public record ForceCode<A>(Literal<U<A>>thunk) implements Code<A> {
    public static <A> Code<A> of(Literal<U<A>> thunk) {
        if (thunk instanceof ThunkLiteral<A> forcer) {
            return forcer.code();
        }
        return new ForceCode<A>(thunk);
    }

    @Override
    public Type<A> type() {
        throw null;
    }

    @Override
    public U<A> interpret(InterpreterEnvironment environment) {
        return thunk.interpret(environment);
    }

    @Override
    public String toString() {
        return "!" + thunk;
    }
}
package com.sstewartgallus.plato.cbpv;

import com.sstewartgallus.plato.runtime.U;
import com.sstewartgallus.plato.syntax.type.Type;

public record ThunkLiteral<A>(Code<A>code) implements Literal<U<A>> {
    public static <A> Literal<U<A>> of(Code<A> code) {
        if (code instanceof ForceCode) {
            var forcer = (ForceCode<A>)code;
            return forcer.thunk();
        }
        return new ThunkLiteral<A>(code);
    }

    @Override
    public Type<U<A>> type() {
        return code.type().thunk();
    }

    @Override
    public U<A> interpret(InterpreterEnvironment environment) {
        return code.interpret(environment);
    }

}
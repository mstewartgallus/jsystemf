package com.sstewartgallus.plato.cbpv;

import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.U;
import com.sstewartgallus.plato.syntax.type.Type;

public record ReturnCode<A>(Literal<A>literal) implements Code<F<A>> {
    public static <A> ReturnCode<A> of(Literal<A> literal) {
        return new ReturnCode<A>(literal);
    }

    @Override
    public Type<F<A>> type() {
        throw null;
    }

    @Override
    public F<A> interpret(InterpreterEnvironment environment) {
        var lit = literal.interpret(environment);
        return () -> lit;
    }

}

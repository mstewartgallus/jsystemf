package com.sstewartgallus.plato.cbpv;

import com.sstewartgallus.plato.syntax.type.Type;
import com.sstewartgallus.plato.syntax.type.TypeCheckException;

public interface Literal<A> {
    Type<A> type() throws TypeCheckException;

    default A interpret(InterpreterEnvironment environment) {
        throw new UnsupportedOperationException(getClass().toString());
    }

    default void compile(CompilerEnvironment environment) {
        throw new UnsupportedOperationException(getClass().toString());
    }
}


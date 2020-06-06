package com.sstewartgallus.plato.cbpv;

import com.sstewartgallus.plato.runtime.Jit;
import com.sstewartgallus.plato.runtime.U;
import com.sstewartgallus.plato.syntax.term.Constraints;
import com.sstewartgallus.plato.syntax.type.Type;

import java.io.PrintWriter;

public interface Code<A> {
    static <A> U<A> compile(Code<A> code, PrintWriter writer) {
        return Jit.jit(code, writer);
    }

    Type<A> type();

    default Constraints findConstraints() {
        throw new UnsupportedOperationException(getClass().toString());
    }

    default U<A> interpret(InterpreterEnvironment environment) {
        throw new UnsupportedOperationException(getClass().toString());
    }

    default void compile(CompilerEnvironment environment) {
        throw new UnsupportedOperationException(getClass().toString());
    }
}


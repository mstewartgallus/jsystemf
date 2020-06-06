package com.sstewartgallus.plato.java;

import com.sstewartgallus.plato.cbpv.CompilerEnvironment;
import com.sstewartgallus.plato.cbpv.InterpreterEnvironment;
import com.sstewartgallus.plato.cbpv.Literal;
import com.sstewartgallus.plato.syntax.type.NominalType;
import com.sstewartgallus.plato.syntax.type.Type;
import com.sstewartgallus.plato.syntax.type.TypeCheckException;

public record IntLiteral(int value) implements Literal<Integer> {
    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public Type<Integer> type() throws TypeCheckException {
        return NominalType.ofTag(new PrimTag<>(int.class));
    }

    @Override
    public Integer interpret(InterpreterEnvironment environment) {
        return value;
    }

    @Override
    public void compile(CompilerEnvironment environment) {
        environment.method().visitLdcInsn(value);
    }
}
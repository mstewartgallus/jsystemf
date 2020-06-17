package com.sstewartgallus.plato.java;

import com.sstewartgallus.plato.ir.cbpv.Literal;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.Jit;

public record IntLiteral(int value) implements Literal<Integer> {
    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public TypeDesc<Integer> type() {
        return IntType.INT_TYPE;
    }

    @Override
    public void compile(Jit.Environment environment) {
        environment.local().methodVisitor().visitLdcInsn(value);
    }
}
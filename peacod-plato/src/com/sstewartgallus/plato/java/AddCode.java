package com.sstewartgallus.plato.java;

import com.sstewartgallus.plato.cbpv.Code;
import com.sstewartgallus.plato.cbpv.CompilerEnvironment;
import com.sstewartgallus.plato.cbpv.InterpreterEnvironment;
import com.sstewartgallus.plato.cbpv.Literal;
import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.U;
import com.sstewartgallus.plato.syntax.type.Type;
import com.sstewartgallus.plato.syntax.type.TypeCheckException;
import org.objectweb.asm.Opcodes;

public record AddCode(Literal<Integer>left, Literal<Integer>right) implements Code<F<Integer>> {
    @Override
    public String toString() {
        return left + "\n" + right + "\n+";
    }

    @Override
    public Type<F<Integer>> type() throws TypeCheckException {
        return IntType.INT_TYPE.unboxed();
    }

    @Override
    public U<F<Integer>> interpret(InterpreterEnvironment environment) {
        var x = left.interpret(environment);
        var y = right.interpret(environment);
        return new IntAction(x + y);
    }

    @Override
    public void compile(CompilerEnvironment environment) {
        left.compile(environment);
        right.compile(environment);
        environment.method().visitInsn(Opcodes.IADD);
    }
}
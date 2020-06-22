package com.sstewartgallus;

import com.sstewartgallus.plato.ir.Global;
import com.sstewartgallus.plato.ir.Label;
import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.cps.ConstantValue;
import com.sstewartgallus.plato.ir.cps.Kont;
import com.sstewartgallus.plato.ir.cps.Value;
import org.objectweb.asm.MethodVisitor;

public class Compiler {
    static <A> A compile(Value<A> value) {
        var environment = new Environment(null);
        compile(environment, value);

        throw null;
    }

    static <A> void compile(Environment environment, Value<A> value) {
        if (value instanceof ConstantValue intValue) {
            System.err.println("LDC " + intValue);

            // freeVariables.method().visitLdcInsn(intValue.stack());
            return;
        }

        throw new IllegalArgumentException(value.getClass().toString());
    }

    static <A> void compile(Environment environment, Kont<A> kont) {
        var label = new org.objectweb.asm.Label();
        environment.method.visitLabel(new org.objectweb.asm.Label());

        throw new IllegalArgumentException(kont.getClass().toString());
    }

    record Environment(MethodVisitor method) {
        public <A> void store(Variable<A> variable) {
            // fixme... lookup label index/type.
            System.err.println("ASTORE " + variable);
            //  method.visitVarInsn(Opcodes.ASTORE, 4);
        }

        public void call(Global<?> global) {
            System.err.println("INDY " + global);
        }

        public <A> void jumpTo(Label<A> label) {
            System.err.println("GOTO " + label);
        }
    }
}

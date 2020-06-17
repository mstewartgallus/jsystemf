package com.sstewartgallus;

import com.sstewartgallus.plato.ir.cps.*;
import com.sstewartgallus.plato.ir.systemf.Global;
import com.sstewartgallus.plato.ir.systemf.Variable;
import com.sstewartgallus.plato.java.IntValue;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class Compiler {
    static <A> A compile(Value<A> value) {
        var environment = new Environment(null);
        compile(environment, value);

        throw null;
    }

    static <A> void compile(Environment environment, Value<A> value) {
        if (value instanceof IntValue intValue) {
            System.err.println("LDC " + intValue);

            // freeVariables.method().visitLdcInsn(intValue.action());
            return;
        }

        if (value instanceof ThunkValue<?> thunkValue) {
            // fixme... create kont then box it/
            throw null;
        }
        throw new IllegalArgumentException(value.getClass().toString());
    }

    static <A> void compile(Environment environment, Kont<A> kont) {
        var label = new Label();
        environment.method.visitLabel(new Label());

        if (kont instanceof ApplyKont<?> applyStack) {
//            compile(freeVariables, applyStack.action());
            //          compile(freeVariables, applyStack.tail());
            throw null;
        }
        if (kont instanceof GotoKont<A> gotoStack) {
            environment.jumpTo(gotoStack.label());
            return;
        }
        throw new IllegalArgumentException(kont.getClass().toString());
    }

    record Environment(MethodVisitor method) {
        public <A> void store(Variable<A> variable) {
            // fixme... lookup variable index/type.
            System.err.println("ASTORE " + variable);
            //  method.visitVarInsn(Opcodes.ASTORE, 4);
        }

        public void call(Global<?> global) {
            System.err.println("INDY " + global);
        }

        public <A> void jumpTo(Lbl<A> label) {
            System.err.println("GOTO " + label);
        }
    }
}

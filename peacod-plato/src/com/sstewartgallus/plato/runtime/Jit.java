package com.sstewartgallus.plato.runtime;

import com.sstewartgallus.plato.ir.cbpv.Code;
import com.sstewartgallus.plato.ir.cbpv.Literal;
import com.sstewartgallus.plato.ir.cps.Lbl;
import com.sstewartgallus.plato.ir.systemf.Variable;
import com.sstewartgallus.plato.runtime.internal.AnonClassLoader;
import com.sstewartgallus.plato.runtime.internal.AsmUtils;
import com.sstewartgallus.plato.runtime.internal.SupplierClassValue;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.constant.ClassDesc;
import java.lang.constant.DynamicCallSiteDesc;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.invoke.MethodType.methodType;

public class Jit {
    // fixme... register the jit inside the runtime....
    static final SupplierClassValue<Table> JIT_CLASSES = new SupplierClassValue<>(Table::new);


    protected Jit() {
    }

    public static <A> U<A> jit(Code<A> instr, MethodHandles.Lookup lookup, PrintWriter writer) {

        var str = new StringWriter();
        writer = new PrintWriter(str);
        var cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        var cv = new TraceClassVisitor(cw, writer);

        var myname = org.objectweb.asm.Type.getInternalName(Jit.class);
        var newclassname = myname + "Impl";

        var thisClass = ClassDesc.of(newclassname.replace('/', '.'));

        cv.visit(Opcodes.V14, Opcodes.ACC_FINAL | Opcodes.ACC_PUBLIC, newclassname, null, org.objectweb.asm.Type.getInternalName(Jit.class), null);

        {
            var mw = cv.visitMethod(Opcodes.ACC_STATIC, "<clinit>", methodType(void.class).descriptorString(), null, null);
            mw.visitCode();

            mw.visitMethodInsn(Opcodes.INVOKESTATIC, org.objectweb.asm.Type.getInternalName(MethodHandles.class), "lookup", methodType(MethodHandles.Lookup.class).descriptorString(), false);
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, myname, "register", methodType(void.class, MethodHandles.Lookup.class).descriptorString(), false);

            mw.visitInsn(Opcodes.RETURN);
            mw.visitMaxs(0, 0);
            mw.visitEnd();
        }

        var method = cv.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "theMethod", methodType(void.class).descriptorString(), null, null);
        method.visitCode();
        var env = new Environment(lookup, thisClass, cv, new ArrayList<>(), new LocalEnv(List.of(), null, method));

        instr.compile(env);

        method.visitMaxs(0, 0);
        method.visitEnd();
        cv.visitEnd();
        System.err.println(str);
        if (true) throw null;


        var bytes = cw.toByteArray();

        var definedClass = AnonClassLoader.defineClass(Jit.class.getClassLoader(), bytes);

        var jitLookup = JIT_CLASSES.get(definedClass).lookup;
/*
        MethodHandle mh;
        try {
            mh = jitLookup.findStatic(definedClass, startMh.methodName(), (MethodType) startMh.invocationType().resolveConstantDesc(lookup));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        var info = jitLookup.revealDirect(mh);
        try {
            return new JitStatic<A>(info, instr.type().resolveConstantDesc(jitLookup), mh);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        } */
        throw null;
    }

    @SuppressWarnings("unused")
    public static void register(MethodHandles.Lookup lookup) {
        JIT_CLASSES.get(lookup.lookupClass()).lookup = lookup;
    }

    interface Arguments<C, A> {
        record Nil<A>() implements Arguments<F<A>, A> {
        }

        record Push<C, A, B>(Literal<A>head, Arguments<C, B>tail) implements Arguments<C, A> {
        }
    }


    static class Table {
        MethodHandles.Lookup lookup;
    }

    public record LocalEnv(List<Variable<?>>arguments, Lbl<?>label, MethodVisitor methodVisitor) {
        public void indy(DynamicCallSiteDesc desc) {
            var args = Arrays.stream(desc.bootstrapArgs()).map(AsmUtils::toAsm).toArray();
            methodVisitor.visitInvokeDynamicInsn(desc.invocationName(), desc.invocationType().descriptorString(),
                    AsmUtils.toHandle(desc.bootstrapMethod()),
                    args);
        }
    }

    public record Environment(MethodHandles.Lookup lookup, ClassDesc thisClass, ClassVisitor cv,
                              List<Variable<?>>freeVariables, LocalEnv local) {

        public <A> void freeVariable(Variable<A> binder) {
            freeVariables.add(binder);
        }

        public <A> Environment frame(Lbl<A> label) {
            var method = cv.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, label.name(), methodType(void.class).descriptorString(), null, null);
            method.visitCode();
            return new Environment(lookup, thisClass, cv, List.of(), new LocalEnv(freeVariables, label, method));
        }

        public <A> void store(Variable<A> binder) {
            System.err.println("fixme store local");
        }

        public <A> void load(Variable<A> variable) {
            System.err.println("fixme load local");
        }

        public <A> void jump(Lbl<A> label) {
            System.err.println("implement jumps");
        }
    }
}

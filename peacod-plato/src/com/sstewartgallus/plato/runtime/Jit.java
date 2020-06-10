package com.sstewartgallus.plato.runtime;

import com.sstewartgallus.plato.ir.cps.CompilerEnvironment;
import com.sstewartgallus.plato.runtime.internal.AnonClassLoader;
import com.sstewartgallus.plato.runtime.internal.SupplierClassValue;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.lang.constant.ClassDesc;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;

import static java.lang.invoke.MethodType.methodType;

public class Jit {
    // fixme... register the jit inside the runtime....
    static final SupplierClassValue<Table> JIT_CLASSES = new SupplierClassValue<>(Table::new);

    protected Jit() {
    }

    public static <A> A jit(Consumer<CompilerEnvironment> consumer, MethodHandles.Lookup lookup, PrintWriter writer) {
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

        {
            var mw = cv.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "get", methodType(U.class).descriptorString(), null, null);
            mw.visitCode();

            consumer.accept(new CompilerEnvironment(lookup, thisClass, cv, mw));

            mw.visitInsn(Opcodes.ARETURN);
            mw.visitMaxs(0, 0);
            mw.visitEnd();
        }

        cv.visitEnd();

        var bytes = cw.toByteArray();

        var definedClass = AnonClassLoader.defineClass(Jit.class.getClassLoader(), bytes);
        MethodHandle mh;
        try {
            mh = JIT_CLASSES.get(definedClass).lookup.findStatic(definedClass, "get", methodType(U.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        try {
            return (A) mh.invoke();
        } catch (Error | RuntimeException e) {
            throw e;
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @SuppressWarnings("unused")
    public static void register(MethodHandles.Lookup lookup) {
        JIT_CLASSES.get(lookup.lookupClass()).lookup = lookup;
    }

    static class Table {
        MethodHandles.Lookup lookup;
    }

}

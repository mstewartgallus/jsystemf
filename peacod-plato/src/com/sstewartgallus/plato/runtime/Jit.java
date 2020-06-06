package com.sstewartgallus.plato.runtime;

import com.sstewartgallus.plato.cbpv.Code;
import com.sstewartgallus.plato.cbpv.CompilerEnvironment;
import com.sstewartgallus.plato.runtime.internal.AnonClassLoader;
import com.sstewartgallus.plato.runtime.internal.SupplierClassValue;
import com.sstewartgallus.plato.syntax.term.Term;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.lang.constant.ClassDesc;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodType.methodType;

public class Jit {
    static class Table {
        MethodHandles.Lookup lookup;
    }
    // fixme... register the jit inside the runtime....
    static final SupplierClassValue<Table> JIT_CLASSES = new SupplierClassValue<>(Table::new);

    protected Jit() {
    }

   public static <A> U<A> jit(Code<A> code, PrintWriter writer) {
        // fixme... privatise as much as possible...
        var cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        var cv = new TraceClassVisitor(cw, writer);

        var myname = org.objectweb.asm.Type.getInternalName(Jit.class);
        var newclassname = myname + "Impl";

        var thisClass = ClassDesc.of(newclassname.replace('/', '.'));

        cv.visit(Opcodes.V14, Opcodes.ACC_FINAL | Opcodes.ACC_PUBLIC, newclassname, null, org.objectweb.asm.Type.getInternalName(Object.class), null);

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
            var mw = cv.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "get", methodType(Term.class).descriptorString(), null, null);
            mw.visitCode();

            code.compile(new CompilerEnvironment(thisClass, cv, mw));

            mw.visitInsn(Opcodes.ARETURN);
            mw.visitMaxs(0, 0);
            mw.visitEnd();
        }

        cv.visitEnd();

        var bytes = cw.toByteArray();

        var definedClass = AnonClassLoader.defineClass(Jit.class.getClassLoader(), bytes);
        MethodHandle mh;
        try {
            mh = JIT_CLASSES.get(definedClass).lookup.findStatic(definedClass, "get", methodType(Term.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        try {
            return (U<A>) mh.invoke();
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

}

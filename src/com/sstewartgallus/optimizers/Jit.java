package com.sstewartgallus.optimizers;

import com.sstewartgallus.plato.LambdaTerm;
import com.sstewartgallus.plato.Term;
import com.sstewartgallus.runtime.AnonClassLoader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.lang.constant.ClassDesc;
import java.lang.invoke.MethodHandle;
import java.util.Map;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;
import static org.objectweb.asm.Opcodes.*;

public final class Jit {
    private Jit() {
    }

    public static <A> Term<A> jit(Term<A> root, PrintWriter writer) {

        // fixme... privatise as much as possible...
        var cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        var cv = new TraceClassVisitor(cw, writer);

        var myname = org.objectweb.asm.Type.getInternalName(LambdaTerm.class);
        var newclassname = myname + "Impl";

        var thisClass = ClassDesc.of(newclassname.replace('/', '.'));

        cv.visit(V14, ACC_FINAL | ACC_PUBLIC, newclassname, null, myname, null);

        // fixme... ldc?
        {
            var mw = cv.visitMethod(ACC_PUBLIC | ACC_STATIC, "get", methodType(Term.class).descriptorString(), null, null);
            mw.visitCode();
            // fixme... conversions...
            root.jit(thisClass, cv, mw, Map.of());
            mw.visitInsn(ARETURN);
            mw.visitMaxs(0, 0);
            mw.visitEnd();
        }

        cv.visitEnd();

        var bytes = cw.toByteArray();

        var definedClass = AnonClassLoader.defineClass(LambdaTerm.class.getClassLoader(), bytes);
        MethodHandle mh;
        try {
            mh = lookup().findStatic(definedClass, "get", methodType(Term.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        try {
            return (Term) mh.invoke();
        } catch (Error | RuntimeException e) {
            throw e;
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

}

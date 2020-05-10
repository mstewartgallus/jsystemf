package com.sstewartgallus.runtime;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandle;
import java.util.List;

import static java.lang.invoke.MethodHandles.Lookup;
import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;
import static org.objectweb.asm.Opcodes.*;

public abstract class Static<T> extends FunValue<T> {

    private static final Handle BOOTSTRAP = new Handle(H_INVOKESTATIC, Type.getInternalName(Static.class), "bootstrapInfoTable",
            methodType(Infotable.class, Lookup.class, String.class, Class.class).descriptorString(),
            false);
    private static final ConstantDynamic INFO_BOOTSTRAP = new ConstantDynamic("infoTable", Infotable.class.descriptorString(), BOOTSTRAP);

    @SuppressWarnings("unused")
    protected static Infotable bootstrapInfoTable(Lookup lookup, String name, Class<?> klass) {
        return (Infotable) ((AnonClassLoader<?>) lookup.lookupClass().getClassLoader()).getValue();
    }

    public static Static<?> spin(List<Class<?>> arguments, Class<?> result, MethodHandle entryPoint) {
        var myname = Type.getInternalName(Static.class);
        var newclassname = myname + "Impl";

        var cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(V14, ACC_FINAL | ACC_PRIVATE, newclassname, null, myname, null);

        cw.visitField(ACC_PUBLIC | ACC_FINAL | ACC_STATIC, "SINGLE", Type.getObjectType(newclassname).getDescriptor(), null, null)
                .visitEnd();

        {
            var mw = cw.visitMethod(ACC_PRIVATE | ACC_STATIC, "<clinit>", methodType(void.class).descriptorString(), null, null);
            mw.visitCode();
            mw.visitTypeInsn(NEW, newclassname);
            mw.visitInsn(DUP);
            mw.visitMethodInsn(INVOKESPECIAL, newclassname, "<init>", methodType(void.class).descriptorString(), false);
            mw.visitFieldInsn(PUTSTATIC, newclassname, "SINGLE", Type.getObjectType(newclassname).getDescriptor());
            mw.visitInsn(RETURN);
            mw.visitMaxs(0, 0);
            mw.visitEnd();
        }

        {
            var mw = cw.visitMethod(ACC_PRIVATE, "<init>", methodType(void.class).descriptorString(), null, null);
            mw.visitCode();
            mw.visitVarInsn(ALOAD, 0);
            mw.visitMethodInsn(INVOKESPECIAL, myname, "<init>", methodType(void.class).descriptorString(), false);
            mw.visitVarInsn(ALOAD, 0);
            mw.visitInsn(RETURN);
            mw.visitMaxs(0, 0);
            mw.visitEnd();
        }

        // fixme... not really a need for dynamically spinning this in the singleton static case...
        {
            var mw = cw.visitMethod(ACC_PUBLIC, "infoTable", methodType(Infotable.class).descriptorString(), null, null);
            mw.visitCode();
            mw.visitLdcInsn(INFO_BOOTSTRAP);
            mw.visitInsn(ARETURN);
            mw.visitMaxs(0, 0);
            mw.visitEnd();
        }

        cw.visitEnd();
        var bytes = cw.toByteArray();

        var definedClass = AnonClassLoader.defineClass(new Infotable(List.of(), arguments, entryPoint), Static.class.getClassLoader(), newclassname.replace('/', '.'), bytes);
        var klass = definedClass.asSubclass(Static.class);

        MethodHandle singleGetter;
        try {
            singleGetter = lookup().findStaticGetter(klass, "SINGLE", klass);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        try {
            return (Static) singleGetter.invoke();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public final String toString() {
        return Static.class.getSimpleName() + ":" + infoTable();
    }
}
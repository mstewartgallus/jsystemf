package com.sstewartgallus.runtime;

import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.support.Guards;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.List;

import static java.lang.invoke.MethodHandles.Lookup;
import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodType.methodType;
import static org.objectweb.asm.Opcodes.*;

public abstract class Static<T> extends FunValue<T> {

    private static final Handle BOOTSTRAP = new Handle(H_INVOKESTATIC, Type.getInternalName(Static.class), "bootstrapInfoTable",
            methodType(Infotable.class, Lookup.class, String.class, Class.class).descriptorString(),
            false);
    private static final ConstantDynamic INFO_BOOTSTRAP = new ConstantDynamic("infoTable", Infotable.class.descriptorString(), BOOTSTRAP);
    private static final SupplierClassValue<LookupHolder> LOOKUP_MAP = new SupplierClassValue<>(LookupHolder::new);

    @SuppressWarnings("unused")
    protected static Infotable bootstrapInfoTable(Lookup lookup, String name, Class<?> klass) {
        return LOOKUP_MAP.get(lookup.lookupClass()).infotable;
    }

    @SuppressWarnings("unused")
    protected static void register(MethodHandles.Lookup lookup) {
        LOOKUP_MAP.get(lookup.lookupClass()).lookup = lookup;
    }

    public static Static<?> spin(List<Class<?>> arguments, Class<?> result, MethodHandle entryPoint) {
        var myname = Type.getInternalName(Static.class);
        var newclassname = myname + "Impl";

        var cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(V14, ACC_FINAL | ACC_PRIVATE, newclassname, null, myname, null);

        {
            var mw = cw.visitMethod(ACC_PRIVATE | ACC_STATIC, "<clinit>", methodType(void.class).descriptorString(), null, null);
            mw.visitCode();

            mw.visitMethodInsn(INVOKESTATIC, Type.getInternalName(MethodHandles.class), "lookup", methodType(MethodHandles.Lookup.class).descriptorString(), false);
            mw.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Static.class), "register", methodType(void.class, MethodHandles.Lookup.class).descriptorString(), false);

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

        var definedClass = AnonClassLoader.defineClass(Static.class.getClassLoader(), bytes);
        var klass = definedClass.asSubclass(Static.class);

        var privateLookup = LOOKUP_MAP.get(klass).lookup;
        LOOKUP_MAP.get(klass).infotable = new Infotable(List.of(), arguments, entryPoint);

        MethodHandle con;
        try {
            con = privateLookup.findConstructor(klass, methodType(void.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        try {
            return (Static) con.invoke();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    protected int arity() {
        return infoTable().arguments().size();
    }

    protected GuardedInvocation saturatedApplication(LinkRequest linkRequest, LinkerServices linkerServices) throws NoSuchFieldException, IllegalAccessException {
        var metadata = infoTable();

        var argument = metadata.arguments();

        var mh = metadata.entryPoint();
        mh = dropArguments(mh, 0, Value.class, Void.class);

        return new GuardedInvocation(mh, Guards.isOfClass(getClass(), mh.type().changeReturnType(boolean.class)));
    }

    public final String toString() {
        return Static.class.getSimpleName() + ":" + infoTable();
    }

    private final static class LookupHolder {
        Lookup lookup;
        Infotable infotable;
    }
}


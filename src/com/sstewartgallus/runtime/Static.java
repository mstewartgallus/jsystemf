package com.sstewartgallus.runtime;

import jdk.dynalink.*;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.support.Guards;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.methodType;
import static org.objectweb.asm.Opcodes.*;

public abstract class Static<T> extends Value<T> {

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

        // fixme... no need for dynamically spinning this in the singleton static case...
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

    // fixme... look furether into   https://gitlab.haskell.org/ghc/ghc/-/wikis/commentary/rts/haskell-execution/function-calls
    final GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices) throws NoSuchFieldException, IllegalAccessException {
        Operation operation = linkRequest.getCallSiteDescriptor().getOperation();
        Object name = null;
        boolean wasNamed = false;
        if (operation instanceof NamedOperation named) {
            name = named.getName();
            wasNamed = true;
            operation = named.getBaseOperation();
        }
        Namespace[] namespaces = null;
        if (operation instanceof NamespaceOperation namespaced) {
            namespaces = namespaced.getNamespaces();
            operation = namespaced.getBaseOperation();
        }

        if (operation instanceof StandardOperation standard) {
            if (standard == StandardOperation.CALL) {
                return getStandardCall(linkRequest, linkerServices);
            }
        }
        return null;
    }

    private GuardedInvocation getStandardCall(LinkRequest linkRequest, LinkerServices linkerServices) throws NoSuchFieldException, IllegalAccessException {
        var metadata = infoTable();

        var cs = linkRequest.getCallSiteDescriptor();
        var calledWithParams = cs.getMethodType().parameterCount() - 2;

        var arity = 1;

        if (calledWithParams < arity) {
            return partialApplication(metadata, calledWithParams, linkRequest, linkerServices);
        }

        if (calledWithParams > arity) {
            return superSaturatedApplication(metadata, linkRequest, linkerServices);
        }

        return saturatedApplication(metadata, linkRequest, linkerServices);
    }

    private GuardedInvocation superSaturatedApplication(Infotable metadata, LinkRequest linkRequest, LinkerServices linkerServices) {
        throw new UnsupportedOperationException("unimplemented");
    }

    private GuardedInvocation partialApplication(Infotable metadata, int calledWith, LinkRequest linkRequest, LinkerServices linkerServices) {
        throw new UnsupportedOperationException("unimplemented");
    }

    private GuardedInvocation saturatedApplication(Infotable metadata, LinkRequest linkRequest, LinkerServices linkerServices) throws NoSuchFieldException, IllegalAccessException {
        var argument = metadata.argument();
        var environment = metadata.environment();

        var mh = metadata.entryPoint();

        // fit into the stupid dummy receiver thing...
        mh = dropArguments(mh, 1, Void.class);

        // fixme... also guard on thunk being not fully saturated...
        mh = mh.asType(mh.type().changeParameterType(0, Value.class));
        return new GuardedInvocation(mh, Guards.isOfClass(getClass(), mh.type().changeReturnType(boolean.class)));
    }

    public abstract Infotable infoTable();

    public final String toString() {
        // fixme... setup metadata for the originator of the Static

        var str = Static.class.getSimpleName();
        // fixme.. extremely slow reflection based toString
        str += Arrays
                .stream(getClass().getFields())
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .sorted(Comparator.comparing(Field::getName))
                .map(f -> {
                    try {
                        return f.get(this);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
        return str;
    }
}
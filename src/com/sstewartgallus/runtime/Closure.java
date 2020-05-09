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
import java.util.stream.Collectors;

import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.methodType;
import static org.objectweb.asm.Opcodes.*;

public abstract class Closure<T> extends Value<T> {

    private static final Handle BOOTSTRAP = new Handle(H_INVOKESTATIC, Type.getInternalName(Closure.class), "bootstrapInfoTable",
            methodType(Infotable.class, Lookup.class, String.class, Class.class).descriptorString(),
            false);
    private static final ConstantDynamic INFO_BOOTSTRAP = new ConstantDynamic("infoTable", Infotable.class.descriptorString(), BOOTSTRAP);

    @SuppressWarnings("unused")
    protected static Infotable bootstrapInfoTable(Lookup lookup, String name, Class<?> klass) {
        return (Infotable) ((AnonClassLoader<?>) lookup.lookupClass().getClassLoader()).getValue();
    }

    // fixme... cache comon frames?
    public static MethodHandle spinFactory(Class<?> environment, Class<?> argument, MethodHandle entryPoint) {
        var myname = Type.getInternalName(Closure.class);
        var newclassname = myname + "Impl";

        // fixme... privatise as much as possible...
        var cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(V14, ACC_FINAL | ACC_PUBLIC, newclassname, null, myname, null);

        {
            var mw = cw.visitMethod(ACC_PUBLIC, "<init>", methodType(void.class, environment).descriptorString(), null, null);
            mw.visitCode();
            mw.visitVarInsn(ALOAD, 0);
            mw.visitMethodInsn(INVOKESPECIAL, myname, "<init>", methodType(void.class).descriptorString(), false);
            mw.visitVarInsn(ALOAD, 0);

            if (environment.isPrimitive()) {
                switch (environment.getName()) {
                    case "boolean", "byte", "char", "short", "int" -> mw.visitVarInsn(ILOAD, 1);
                    case "long" -> mw.visitVarInsn(LLOAD, 1);
                    case "float" -> mw.visitVarInsn(FLOAD, 1);
                    case "double" -> mw.visitVarInsn(DLOAD, 1);
                    default -> throw new IllegalStateException(environment.getName());
                }
            } else {
                mw.visitVarInsn(ALOAD, 1);
            }
            mw.visitFieldInsn(PUTFIELD, newclassname, "environment", environment.descriptorString());
            mw.visitInsn(RETURN);
            mw.visitMaxs(0, 0);
            mw.visitEnd();
        }

        {
            var mw = cw.visitMethod(ACC_PUBLIC, "infoTable", methodType(Infotable.class).descriptorString(), null, null);
            mw.visitCode();
            mw.visitLdcInsn(INFO_BOOTSTRAP);
            mw.visitInsn(ARETURN);
            mw.visitMaxs(0, 0);
            mw.visitEnd();
        }

        // fixme... make private if possible...

        cw.visitField(ACC_PUBLIC, "environment", environment.descriptorString(), null, null)
                .visitEnd();

        cw.visitEnd();
        var bytes = cw.toByteArray();

        var definedClass = AnonClassLoader.defineClass(new Infotable(environment, argument, entryPoint), Closure.class.getClassLoader(), newclassname.replace('/', '.'), bytes);
        var klass = definedClass.asSubclass(Closure.class);

        MethodHandle con;
        try {
            con = lookup().findConstructor(klass, methodType(void.class, environment));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return con.asType(con.type().changeReturnType(Closure.class));
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
        var execute = metadata.entryPoint();

        var environmentGetter = lookup().findGetter(getClass(), "environment", environment);
        var mh = filterArguments(execute, 0, environmentGetter);

        // fit into the stupid dummy receiver thing...
        mh = dropArguments(mh, 1, Void.class);

        // fixme... also guard on thunk being not fully saturated...
        mh = mh.asType(mh.type().changeParameterType(0, Value.class));
        return new GuardedInvocation(mh, Guards.isOfClass(getClass(), mh.type().changeReturnType(boolean.class)));
    }

    public abstract Infotable infoTable();

    public final String toString() {
        // fixme... setup metadata for the originator of the closure

        var str = Closure.class.getSimpleName();
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

record Infotable(Class<?>environment,
                 Class<?>argument,
                 MethodHandle entryPoint) {
}
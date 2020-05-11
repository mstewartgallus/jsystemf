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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.methodType;
import static org.objectweb.asm.Opcodes.*;

// fixme... break out into another class for environment capturing closures...
public abstract class Closure<T> extends FunValue<T> {
    private static final Handle BOOTSTRAP = new Handle(H_INVOKESTATIC, Type.getInternalName(Closure.class), "bootstrapInfoTable",
            methodType(Infotable.class, Lookup.class, String.class, Class.class).descriptorString(),
            false);
    private static final ConstantDynamic INFO_BOOTSTRAP = new ConstantDynamic("infoTable", Infotable.class.descriptorString(), BOOTSTRAP);
    private static final SupplierClassValue<LookupHolder> LOOKUP_MAP = new SupplierClassValue<>(LookupHolder::new);
    private final Object funValue;

    protected Closure(Object value) {
        this.funValue = value;
    }

    @SuppressWarnings("unused")
    protected static Infotable bootstrapInfoTable(Lookup lookup, String name, Class<?> klass) {
        return LOOKUP_MAP.get(lookup.lookupClass()).infotable;
    }

    @SuppressWarnings("unused")
    protected static void register(MethodHandles.Lookup lookup) {
        LOOKUP_MAP.get(lookup.lookupClass()).lookup = lookup;
    }

    // fixme... cache common frames?
    public static MethodHandle spinFactory(List<Class<?>> environments, List<Class<?>> arguments, MethodHandle entryPoint) {
        // fixme....
        var args = new ArrayList<Class<?>>(environments.size() + 1);
        args.add(Object.class);
        args.addAll(environments);

        var myname = Type.getInternalName(Closure.class);
        var newclassname = myname + "Impl";

        // fixme... privatise as much as possible...
        var cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(V14, ACC_FINAL | ACC_PRIVATE, newclassname, null, myname, null);

        {
            var mw = cw.visitMethod(ACC_PRIVATE | ACC_STATIC, "<clinit>", methodType(void.class).descriptorString(), null, null);
            mw.visitCode();

            mw.visitMethodInsn(INVOKESTATIC, Type.getInternalName(MethodHandles.class), "lookup", methodType(MethodHandles.Lookup.class).descriptorString(), false);
            mw.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Closure.class), "register", methodType(void.class, MethodHandles.Lookup.class).descriptorString(), false);

            mw.visitInsn(RETURN);
            mw.visitMaxs(0, 0);
            mw.visitEnd();
        }

        // fixme... consider using methodhandles for passing in the environment...
        {
            var mw = cw.visitMethod(ACC_PRIVATE, "<init>", methodType(void.class, args).descriptorString(), null, null);
            mw.visitCode();
            mw.visitVarInsn(ALOAD, 0);
            mw.visitVarInsn(ALOAD, 1);
            mw.visitMethodInsn(INVOKESPECIAL, myname, "<init>", methodType(void.class, Object.class).descriptorString(), false);
            mw.visitVarInsn(ALOAD, 0);

            var ii = 2;
            var envField = 0;
            for (var env : environments) {
                if (env.isPrimitive()) {
                    switch (env.getName()) {
                        case "boolean", "byte", "char", "short", "int" -> {
                            mw.visitVarInsn(ILOAD, ii);
                            ii += 1;
                        }
                        case "long" -> {
                            mw.visitVarInsn(LLOAD, ii);
                            ii += 2;
                        }
                        case "float" -> {
                            mw.visitVarInsn(FLOAD, ii);
                            ii += 1;
                        }
                        case "double" -> {
                            mw.visitVarInsn(DLOAD, ii);
                            ii += 2;
                        }
                        default -> throw new IllegalStateException(env.getName());
                    }
                } else {
                    mw.visitVarInsn(ALOAD, ii);
                    ii += 1;
                }
                mw.visitFieldInsn(PUTFIELD, newclassname, "e" + envField, env.descriptorString());
                envField += 1;
            }
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

        {
            var ii = 0;
            for (var env : environments) {
                cw.visitField(ACC_PRIVATE, "e" + ii, env.descriptorString(), null, null)
                        .visitEnd();
                ++ii;
            }
        }

        cw.visitEnd();
        var bytes = cw.toByteArray();

        var definedClass = AnonClassLoader.defineClass(Closure.class.getClassLoader(), bytes);
        var klass = definedClass.asSubclass(Closure.class);

        var privateLookup = LOOKUP_MAP.get(klass).lookup;

        LOOKUP_MAP.get(klass).infotable = new Infotable(environments, arguments, entryPoint);

        MethodHandle con;
        try {
            con = privateLookup.findConstructor(klass, methodType(void.class, args));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return con.asType(con.type().changeReturnType(Closure.class));
    }

    protected int arity() {
        return infoTable().arguments().size();
    }

    protected GuardedInvocation saturatedApplication(LinkRequest linkRequest, LinkerServices linkerServices) throws NoSuchFieldException, IllegalAccessException {
        var metadata = infoTable();

        var argument = metadata.arguments();
        var environment = metadata.environment();
        var execute = metadata.entryPoint();

        // fixme... handle environment better...
        var environmentGetter = lookup().findGetter(getClass(), "environment", environment.get(0));
        var mh = filterArguments(execute, 0, environmentGetter);

        // fit into the stupid dummy receiver thing...
        mh = dropArguments(mh, 1, Void.class);
        // fixme... also guard on thunk being not fully saturated...
        mh = mh.asType(mh.type().changeParameterType(0, Value.class));

        return new GuardedInvocation(mh, Guards.isOfClass(getClass(), mh.type().changeReturnType(boolean.class)));
    }

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

    private final static class LookupHolder {
        Lookup lookup;
        Infotable infotable;
    }
}


package com.sstewartgallus.runtime;

import jdk.dynalink.StandardOperation;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;

import static java.lang.invoke.MethodType.methodType;
import static org.objectweb.asm.Opcodes.*;

/**
 * Unfortunately the standard libary MethodHandleProxies creates boxes arguments it passes through with Object[]...
 */
public abstract class TermInvoker<T> {
    private static final Handle BOOTSTRAP = new Handle(H_INVOKESTATIC, Type.getInternalName(TermInvoker.class), "bootstrap",
            methodType(CallSite.class, MethodHandles.Lookup.class, String.class, MethodType.class).descriptorString(),
            false);
    private static final SupplierClassValue<LookupHolder> LOOKUP_MAP = new SupplierClassValue<>(LookupHolder::new);

    // fixme... use private bootstrap?
    @SuppressWarnings("unused")
    protected static CallSite bootstrap(MethodHandles.Lookup lookup, String name, MethodType methodType) {
        var lookupValue = LOOKUP_MAP.get(lookup.lookupClass()).lookupDelegate;
        return TermLinker.link(lookupValue, StandardOperation.CALL, methodType);
    }

    @SuppressWarnings("unused")
    protected static void register(MethodHandles.Lookup lookup) {
        LOOKUP_MAP.get(lookup.lookupClass()).lookup = lookup;
    }

    // fixme... cache comon frames?
    // fixme... separate arguments from simply locals on the locals...
    // fixme.. pass in lookup()?
    private static <I> I spin(MethodHandles.Lookup lookup, Class<I> iface, String methodName, MethodType methodType) {
        var myname = Type.getInternalName(TermInvoker.class);
        var newclassname = myname + "Impl";

        // fixme... privatise as much as possible...
        var cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(V14, ACC_FINAL | ACC_PRIVATE, newclassname, null, myname, new String[]{Type.getInternalName(iface)});

        {
            var mw = cw.visitMethod(ACC_PRIVATE | ACC_STATIC, "<clinit>", methodType(void.class).descriptorString(), null, null);
            mw.visitCode();

            mw.visitMethodInsn(INVOKESTATIC, Type.getInternalName(MethodHandles.class), "lookup", methodType(MethodHandles.Lookup.class).descriptorString(), false);
            mw.visitMethodInsn(INVOKESTATIC, Type.getInternalName(TermInvoker.class), "register", methodType(void.class, MethodHandles.Lookup.class).descriptorString(), false);

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

        {
            var mw = cw.visitMethod(ACC_PUBLIC, methodName, methodType.descriptorString(), null, null);
            mw.visitCode();

            mw.visitVarInsn(ALOAD, 1);
            mw.visitInsn(ACONST_NULL);

            var ii = 2;
            for (var param : methodType.dropParameterTypes(0, 1).parameterList()) {
                if (param.isPrimitive()) {
                    switch (param.getName()) {
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
                        default -> throw new IllegalStateException(param.getName());
                    }
                    continue;
                }

                mw.visitVarInsn(ALOAD, ii);
                ii += 1;
            }

            var indySig = methodType.insertParameterTypes(1, Void.class);
            mw.visitInvokeDynamicInsn(methodName, indySig.descriptorString(), BOOTSTRAP);

            var returnType = methodType.returnType();
            if (returnType.isPrimitive()) {
                switch (returnType.getName()) {
                    case "void" -> mw.visitInsn(RETURN);
                    case "boolean", "byte", "char", "short", "int" -> mw.visitInsn(IRETURN);
                    case "long" -> mw.visitInsn(LRETURN);
                    case "float" -> mw.visitInsn(FRETURN);
                    case "double" -> mw.visitInsn(DRETURN);
                    default -> throw new IllegalStateException(returnType.getName());
                }
            } else {
                mw.visitInsn(ARETURN);
            }

            mw.visitMaxs(0, 0);
            mw.visitEnd();
        }

        cw.visitEnd();
        var bytes = cw.toByteArray();

        var definedClass = AnonClassLoader.defineClass(TermInvoker.class.getClassLoader(), bytes);
        var klass = definedClass.asSubclass(TermInvoker.class);

        var privateLookup = LOOKUP_MAP.get(klass).lookup;
        LOOKUP_MAP.get(klass).lookupDelegate = lookup;

        MethodHandle con;
        try {
            con = privateLookup.findConstructor(klass, methodType(void.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        Object invoker;
        try {
            invoker = con.invoke();
        } catch (Error | RuntimeException e) {
            throw e;
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }

        return iface.cast(invoker);
    }

    public static <I> I newInstance(MethodHandles.Lookup lookup, Class<I> iface) {
        // fixme... cache these with ClassValue? can'domain really do that if you pass in the lookup though!
        // fixme... get a better functional interface check..
        var maybeMethod = Arrays.stream(iface.getMethods()).filter(m -> !m.isSynthetic() && !m.isBridge() && !m.isDefault()).findFirst();
        if (maybeMethod.isEmpty()) {
            throw new IllegalArgumentException("not a functional interface " + iface);
        }

        var method = maybeMethod.get();
        var params = method.getParameterTypes();
        var returnType = method.getReturnType();
        return spin(lookup, iface, method.getName(), methodType(returnType, params));
    }

    private final static class LookupHolder {
        MethodHandles.Lookup lookupDelegate;
        MethodHandles.Lookup lookup;
    }
}


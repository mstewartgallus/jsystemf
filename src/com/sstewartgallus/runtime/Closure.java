package com.sstewartgallus.runtime;

import jdk.dynalink.StandardOperation;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.support.Guards;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.methodType;
import static org.objectweb.asm.Opcodes.*;

// fixme... break out into another class for env capturing closures...
public abstract class Closure<A, B> implements Function<A, B> {
    private static final SupplierClassValue<LookupHolder> LOOKUP_MAP = new SupplierClassValue<>(LookupHolder::new);
    private static final Handle BOOTSTRAP = new Handle(H_INVOKESTATIC,
            Type.getInternalName(Closure.class),
            "bootstrap",
            methodType(CallSite.class, Lookup.class, String.class, MethodType.class).descriptorString(),
            false);

    protected static CallSite bootstrap(Lookup lookup, String name, MethodType methodType) {
        return TermLinker.link(lookup, StandardOperation.CALL, methodType);
    }

    @SuppressWarnings("unused")
    protected static void register(MethodHandles.Lookup lookup) {
        LOOKUP_MAP.get(lookup.lookupClass()).lookup = lookup;
    }

    // fixme... cache common frames?
    public static MethodHandle spinFactory(MethodType methodType) {
        var environment = methodType.parameterList();

        var myname = Type.getInternalName(Closure.class);
        var newclassname = myname + "Impl";

        // fixme... privatise as much as possible...
        var cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(V14, ACC_FINAL | ACC_PRIVATE, newclassname, null, myname, new String[]{Type.getInternalName(Function.class)});

        {
            var mw = cw.visitMethod(ACC_PRIVATE | ACC_STATIC, "<clinit>", methodType(void.class).descriptorString(), null, null);
            mw.visitCode();

            mw.visitMethodInsn(INVOKESTATIC, Type.getInternalName(MethodHandles.class), "lookup", methodType(MethodHandles.Lookup.class).descriptorString(), false);
            mw.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Closure.class), "register", methodType(void.class, MethodHandles.Lookup.class).descriptorString(), false);

            mw.visitInsn(RETURN);
            mw.visitMaxs(0, 0);
            mw.visitEnd();
        }

        {
            var mw = cw.visitMethod(ACC_PRIVATE, "<init>", methodType(void.class, environment).descriptorString(), null, null);
            mw.visitCode();
            mw.visitVarInsn(ALOAD, 0);
            mw.visitMethodInsn(INVOKESPECIAL, myname, "<init>", methodType(void.class).descriptorString(), false);

            var ii = 1;
            var envField = 0;
            for (var env : environment) {
                mw.visitVarInsn(ALOAD, 0);
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
            var mw = cw.visitMethod(ACC_PUBLIC, "apply", methodType(Object.class, Object.class).descriptorString(), null, null);
            mw.visitCode();

            mw.visitVarInsn(ALOAD, 1);
            mw.visitInsn(ACONST_NULL);

            var envField = 0;
            for (var env : environment) {
                mw.visitVarInsn(ALOAD, 0);
                mw.visitFieldInsn(GETFIELD, newclassname, "e" + envField, env.descriptorString());
                envField += 1;
            }

            mw.visitInvokeDynamicInsn("CALL",
                    methodType(Object.class, environment).insertParameterTypes(0, Object.class, Void.class).descriptorString(),
                    BOOTSTRAP);

            mw.visitInsn(ARETURN);
            mw.visitMaxs(0, 0);
            mw.visitEnd();
        }

        {
            var ii = 0;
            for (var env : environment) {
                cw.visitField(ACC_PRIVATE, "e" + ii, env.descriptorString(), null, null)
                        .visitEnd();
                ++ii;
            }
        }

        cw.visitEnd();
        var bytes = cw.toByteArray();

        var definedClass = AnonClassLoader.defineClass(Closure.class.getClassLoader(), bytes);
        var klass = definedClass.asSubclass(Closure.class);

        var holder = LOOKUP_MAP.get(klass);

        var privateLookup = holder.lookup;
        var environmentFields = new ArrayList<MethodHandle>();
        {
            var ii = 0;
            for (var env : environment) {
                MethodHandle field;
                try {
                    field = privateLookup.findGetter(klass, "e" + ii, env);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                environmentFields.add(field);
            }
        }
        holder.info = new Info(environment, environmentFields);

        MethodHandle con;
        try {
            con = privateLookup.findConstructor(klass, methodType(void.class, environment));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return con.asType(con.type().changeReturnType(Closure.class));
    }

    protected int arity() {
        return LOOKUP_MAP.get(getClass()).info.environment().size();
    }

    protected GuardedInvocation saturatedApplication(LinkRequest linkRequest, LinkerServices linkerServices) throws NoSuchFieldException, IllegalAccessException {
        var data = LOOKUP_MAP.get(getClass());
        var info = data.info;

        var environment = info.environment();
        var fields = info.fields();

        var resultType = linkRequest.getCallSiteDescriptor().getMethodType();
        var param1 = resultType.parameterType(0);
        var newArgs = resultType.dropParameterTypes(0, 2).parameterList();

        // fixme... user proper lookup...
        var mh = TermLinker.link(lookup(), StandardOperation.CALL, methodType(resultType.returnType(), environment)).dynamicInvoker();

        MethodHandle[] envGets;
        {
            var ii = 0;
            envGets = new MethodHandle[environment.size()];
            envGets[0] = envGets[1] = empty(methodType(Void.class, param1));
            for (var getter : fields) {
                getter = getter.asType(getter.type().changeParameterType(0, param1));
                envGets[2 + ii] = getter;
                ++ii;
            }
        }
        mh = filterArguments(mh, 0, envGets);

        var reorder = new int[mh.type().parameterCount()];
        for (var ii = 0; ii < 2 + environment.size(); ++ii) {
            reorder[ii] = 0;
        }
        for (var ii = 0; ii < newArgs.size(); ++ii) {
            reorder[2 + environment.size() + ii] = 2 + ii;
        }

        mh = permuteArguments(mh, resultType, reorder);

        // fixme... also guard on thunk being not fully saturated... etc...
        return new GuardedInvocation(mh, Guards.isOfClass(getClass(), mh.type().changeReturnType(boolean.class)));
    }


    private final static class LookupHolder {
        Lookup lookup;
        Info info;
    }

    private record Info(List<Class<?>>environment, List<MethodHandle>fields) {
    }

}


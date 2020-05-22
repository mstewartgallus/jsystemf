package com.sstewartgallus.optimizers;

import com.sstewartgallus.ext.variables.VarTerm;
import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.*;
import com.sstewartgallus.runtime.AnonClassLoader;
import com.sstewartgallus.runtime.SupplierClassValue;
import org.objectweb.asm.*;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.lang.constant.*;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.lang.invoke.MethodType.methodType;

public abstract class Jit {
    // fixme... register the jit inside the runtime....
    private static final SupplierClassValue<Table> JIT_CLASSES = new SupplierClassValue<>(Table::new);

    protected Jit() {
    }

    @SuppressWarnings("unused")
    public static void register(MethodHandles.Lookup lookup) {
        JIT_CLASSES.get(lookup.lookupClass()).lookup = lookup;
    }

    public static <A> Term<A> jit(Term<A> root, PrintWriter writer) {

        // fixme... privatise as much as possible...
        var cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        var cv = new TraceClassVisitor(cw, writer);

        var myname = org.objectweb.asm.Type.getInternalName(Jit.class);
        var newclassname = myname + "Impl";

        var thisClass = ClassDesc.of(newclassname.replace('/', '.'));

        cv.visit(Opcodes.V14, Opcodes.ACC_FINAL | Opcodes.ACC_PUBLIC, newclassname, null, myname, null);

        // fixme... ldc?
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

            // fixme... conversions...
            jit(root, thisClass, cv, mw, Map.of());

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
            return (Term) mh.invoke();
        } catch (Error | RuntimeException e) {
            throw e;
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private static <A> void jit(Term<A> term, ClassDesc thisClass, ClassVisitor cv, MethodVisitor mw, Map<VarTerm<?>, VarData> varMap) {
        if (term instanceof ApplyTerm<?, A> applyTerm) {
            jitApply(applyTerm, thisClass, cv, mw, varMap);
            return;
        }
        if (term instanceof LambdaTerm<?, ?> lambdaTerm) {
            jitLambda(lambdaTerm, thisClass, cv, mw, varMap);
            return;
        }
        // fixme..make VarValue a regular not nominal term again...
        if (term instanceof VarTerm<?> varTerm) {
            jitVar(varTerm, thisClass, cv, mw, varMap);
            return;
        }

        // fixme... just fall back to the interpreter...
        throw new IllegalStateException(term.toString());
    }

    private static void jitVar(VarTerm<?> varTerm, ClassDesc thisClass, ClassVisitor cv, MethodVisitor mw, Map<VarTerm<?>, VarData> varMap) {
        // fixme... doublec check if var
        var data = varMap.get(varTerm);
        var clazz = data.type().erase();
        var ii = data.argument();
        if (clazz.isPrimitive()) {
            switch (clazz.getName()) {
                case "boolean", "byte", "char", "short", "int" -> {
                    mw.visitVarInsn(Opcodes.ILOAD, ii);
                }
                case "long" -> {
                    mw.visitVarInsn(Opcodes.LLOAD, ii);
                }
                case "float" -> {
                    mw.visitVarInsn(Opcodes.FLOAD, ii);
                }
                case "double" -> {
                    mw.visitVarInsn(Opcodes.DLOAD, ii);
                }
                default -> throw new IllegalStateException(clazz.getName());
            }
        } else {
            mw.visitVarInsn(Opcodes.ALOAD, ii);
        }
    }

    private static void jitLambda(LambdaTerm<?, ?> lambdaTerm, ClassDesc thisClass, ClassVisitor cv, MethodVisitor mw, Map<VarTerm<?>, VarData> varMap) {

        var td = lambdaTerm.domain().describeConstable().get();
        var rangeDesc = lambdaTerm.range().describeConstable().get();

        LambdaTerm<?, ?> current = lambdaTerm;

        var args = new ArrayList<Class<?>>();

        Class<?> range;
        Term<?> body;
        var varDataMap = new HashMap<VarTerm<?>, VarData>();
        var ii = 0;
        for (; ; ) {
            var v = new VarTerm(current.domain());
            varDataMap.put(v, new VarData(ii, current.domain()));
            // fixme.. handle longs/doubles
            ++ii;

            body = current.apply(v);
            args.add(current.domain().erase());
            range = body.type().erase();

            if (body instanceof LambdaTerm<?, ?> lambda) {
                current = lambda;
                continue;
            }

            break;
        }

        // fixme... generate unique name...
        var methodName = "apply";

        var methodType = methodType(range, args);
        var methodTypeDesc = methodType.describeConstable().get();

        var mh = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, thisClass, methodName, methodTypeDesc);

        {
            var newMethod = cv.visitMethod(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, methodName, methodType.descriptorString(), null, null);
            newMethod.visitCode();

            jit(body, thisClass, cv, newMethod, varDataMap);

            if (range.isPrimitive()) {
                switch (range.getName()) {
                    case "boolean", "byte", "char", "short", "int" -> {
                        newMethod.visitInsn(Opcodes.IRETURN);
                    }
                    case "long" -> {
                        newMethod.visitInsn(Opcodes.LRETURN);
                    }
                    case "float" -> {
                        newMethod.visitInsn(Opcodes.FRETURN);
                    }
                    case "double" -> {
                        newMethod.visitInsn(Opcodes.DRETURN);
                    }
                    default -> throw new IllegalStateException(range.getName());
                }
            } else {
                newMethod.visitInsn(Opcodes.ARETURN);
            }

            newMethod.visitMaxs(0, 0);
            newMethod.visitEnd();
        }

        var desc = TermDesc.ofMethod(td, rangeDesc, mh);

        mw.visitLdcInsn(AsmUtils.toAsm(desc));
    }

    private static <A, B> void jitApply(ApplyTerm<A, B> applyTerm, ClassDesc thisClass, ClassVisitor cv, MethodVisitor mw, Map<VarTerm<?>, VarData> varMap) {
        var f = applyTerm.f();
        var x = applyTerm.x();

        // fixme.. unroll multiple applications.
        jit(f, thisClass, cv, mw, varMap);

        mw.visitInsn(Opcodes.ACONST_NULL);

        jit(x, thisClass, cv, mw, varMap);

        // fixme....
        var range = ((LambdaTerm<A, B>) f).range();
        var t = range.erase();
        var methodTypeDesc = methodType(t, f.type().erase(), Void.class, x.type().erase()).describeConstable().get();

        var mt = MethodTypeDesc.ofDescriptor(methodType(CallSite.class, MethodHandles.Lookup.class, String.class, MethodType.class).descriptorString());
        var bsm = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, AsmUtils.CD_TermBootstraps, "invoke", mt);
        var indy = DynamicCallSiteDesc.of(bsm, "CALL", methodTypeDesc);

        Handle boot = AsmUtils.toHandle(indy.bootstrapMethod());
        mw.visitInvokeDynamicInsn(indy.invocationName(), indy.invocationType().descriptorString(), boot);
    }

    static class Table {
        MethodHandles.Lookup lookup;
    }

    private static final record VarData(int argument, Type<?>type) {
    }
}

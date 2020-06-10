package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.type.RealType;
import com.sstewartgallus.plato.ir.type.Type;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.ActionDesc;
import com.sstewartgallus.plato.runtime.U;
import com.sstewartgallus.plato.runtime.internal.AsmUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CompilerEnvironment {
    private static final AtomicLong IDS = new AtomicLong(0);
    private final Map<LocalValue<?>, VarData> locals;
    private final Map<LabelValue<?>, LabelData> labels;

    private final MethodVisitor methodVisitor;
    private final ClassVisitor classVisitor;
    private final ClassDesc thisClass;
    private final MethodHandles.Lookup lookup;

    private CompilerEnvironment(MethodHandles.Lookup lookup,
                                Map<LocalValue<?>, VarData> locals,
                                Map<LabelValue<?>, LabelData> labels,
                                ClassDesc thisClass, ClassVisitor classVisitor, MethodVisitor methodVisitor) {
        this.locals = locals;
        this.labels = labels;
        this.lookup = lookup;
        this.thisClass = thisClass;
        this.classVisitor = classVisitor;
        this.methodVisitor = methodVisitor;
    }

    public CompilerEnvironment(MethodHandles.Lookup lookup, ClassDesc thisClass, ClassVisitor classVisitor, MethodVisitor methodVisitor) {
        this.locals = Map.of();
        this.labels = new HashMap<>();
        this.lookup = lookup;
        this.thisClass = thisClass;
        this.classVisitor = classVisitor;
        this.methodVisitor = methodVisitor;
    }

    public <A> void loadLocal(GlobalValue<A> variable) {
        var packageName = variable.packageName();
        var ref = ActionDesc.ofReference(packageName, variable.name(), variable.type());
        methodVisitor.visitLdcInsn(AsmUtils.toAsm(ref));
    }

    public MethodVisitor method() {
        return methodVisitor;
    }

    public <A> void allocLabel(LabelValue<A> label, Set<LocalValue<?>> locals, Consumer<CompilerEnvironment> k) {
        var allThings = Stream.concat(label
                        .environment()
                        .stream(),
                label.arguments().stream())
                .sorted().collect(Collectors.toUnmodifiableList());
        var argTypes = allThings
                .stream()
                .map(v -> ((RealType) resolve(v.type())).erase().describeConstable().get())
                .toArray(ClassDesc[]::new);

        System.err.println("defining " + label);
        var methodType = MethodTypeDesc.of(U.class.describeConstable().get(), argTypes);

        var newLocals = new HashMap<LocalValue<?>, VarData>();
        var ii = 0;
        for (var dependency : label.environment()) {
            newLocals.put(dependency, new VarData(ii++));
        }
        for (var dependency : label.arguments()) {
            newLocals.put(dependency, new VarData(ii++));
        }
        for (var dependency : locals) {
            newLocals.put(dependency, new VarData(ii++));
        }

        // fixme... generate unique name...
        var methodName = label.canonicalName() + "_" + IDS.getAndAdd(1);

        var range = methodType.returnType();

        var mh = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, thisClass, methodName, methodType);
        labels.put(label, new LabelData(mh));

        {
            var newMethod = classVisitor.visitMethod(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, methodName, methodType.descriptorString(), null, null);
            newMethod.visitCode();

            k.accept(new CompilerEnvironment(lookup, newLocals, labels, thisClass, classVisitor, newMethod));

            if (range.isPrimitive()) {
                switch (range.displayName()) {
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
                    default -> throw new IllegalStateException(range.toString());
                }
            } else {
                newMethod.visitInsn(Opcodes.ARETURN);
            }

            newMethod.visitMaxs(0, 0);
            newMethod.visitEnd();
        }

        // fixme.. place mh in reference... right?
    }

    public <A> Type<A> resolve(TypeDesc<A> type) {
        try {
            // fixme... pass on error?
            return type.resolveConstantDesc(lookup);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public <A> void storeLocal(LocalValue<A> binder) {
        var idx = locals.get(binder).index();
        var binderType = (RealType<A>) resolve(binder.type());
        var erasure = binderType.erase();
        if (erasure == int.class) {
            methodVisitor.visitVarInsn(Opcodes.ISTORE, idx);
        } else {
            methodVisitor.visitVarInsn(Opcodes.ASTORE, idx);
        }
    }

    public <A> void loadLabel(LabelValue<A> label) {
        var type = label.type();
        var data = labels.get(label);

        var env = label
                .environment()
                .stream()
                .sorted()
                .toArray(LocalValue[]::new);
        for (var v : env) {
            v.compile(this);
        }

        if (env.length == 0) {
            var desc = ActionDesc.ofMethod(type, data.desc());
            methodVisitor.visitLdcInsn(AsmUtils.toAsm(desc));
            return;
        }

        {
            var envdesc = Arrays.stream(env).map(e -> ((RealType<?>) resolve(e.type())).erase().describeConstable().get()).toArray(ClassDesc[]::new);
            var desc = ActionDesc.ofClosure(type, data.desc(), envdesc);

            var args = Arrays
                    .stream(desc.bootstrapArgs())
                    .map(AsmUtils::toAsm)
                    .toArray(Object[]::new);
            methodVisitor.visitInvokeDynamicInsn(desc.invocationName(),
                    desc.invocationType().descriptorString(),
                    AsmUtils.toHandle(desc.bootstrapMethod()),
                    args);
        }
    }

    public <A> void loadLocal(LocalValue<A> variable) {
        var type = (RealType<A>) resolve(variable.type());

        var data = locals.get(variable);
        if (null == data) {
            throw new Error(variable + " not found in " + locals);
        }

        var clazz = type.erase();
        var ii = data.index();
        if (clazz.isPrimitive()) {
            switch (clazz.getName()) {
                case "boolean", "byte", "char", "short", "int" -> {
                    methodVisitor.visitVarInsn(Opcodes.ILOAD, ii);
                }
                case "long" -> {
                    methodVisitor.visitVarInsn(Opcodes.LLOAD, ii);
                }
                case "float" -> {
                    methodVisitor.visitVarInsn(Opcodes.FLOAD, ii);
                }
                case "double" -> {
                    methodVisitor.visitVarInsn(Opcodes.DLOAD, ii);
                }
                default -> throw new IllegalStateException(clazz.getName());
            }
        } else {
            methodVisitor.visitVarInsn(Opcodes.ALOAD, ii);
        }
    }
}

record LabelData(DirectMethodHandleDesc desc) {
}

record VarData(int index) {
}
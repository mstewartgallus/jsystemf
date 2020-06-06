package com.sstewartgallus.plato.cbpv;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public final class CompilerEnvironment {
    private final Map<VarLiteral<?>, VarData> map;
    private final MethodVisitor methodVisitor;
    private final ClassVisitor classVisitor;
    private final ClassDesc thisClass;

    private CompilerEnvironment(Map<VarLiteral<?>, VarData> map, ClassDesc thisClass, ClassVisitor classVisitor, MethodVisitor methodVisitor) {
        this.map = map;
        this.thisClass = thisClass;
        this.classVisitor = classVisitor;
        this.methodVisitor = methodVisitor;
    }

    public CompilerEnvironment(ClassDesc thisClass, ClassVisitor classVisitor, MethodVisitor methodVisitor) {
        this.map = Map.of();
        this.thisClass = thisClass;
        this.classVisitor = classVisitor;
        this.methodVisitor = methodVisitor;
    }

    public <A> void emitVariable(VarLiteral<A> variable) {
        var data = map.get(variable);
        Objects.requireNonNull(data);
        var type = variable.type();

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

    public MethodVisitor method() {
        return methodVisitor;
    }

    public DirectMethodHandleDesc newMethod(MethodTypeDesc methodType, List<VarLiteral<?>> arguments, Consumer<CompilerEnvironment> k) {
        // fixme... generate unique name...
        var methodName = "apply";

        var argumentMap = new HashMap<VarLiteral<?>, VarData>();
        // fixme... support doubles/longs
        var index = 0;
        for (var argument : arguments) {
            argumentMap.put(argument, new VarData(index++));
        }

        var range = methodType.returnType();

        var mh = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, thisClass, methodName, methodType);

        {
            var newMethod = classVisitor.visitMethod(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, methodName, methodType.descriptorString(), null, null);
            newMethod.visitCode();

            k.accept(new CompilerEnvironment(argumentMap, thisClass, classVisitor, newMethod));

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

        return mh;
    }
}

record VarData(int index) {
}
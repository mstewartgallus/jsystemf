package com.sstewartgallus.ext.variables;

import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.TypeCheckException;
import com.sstewartgallus.plato.ValueTerm;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.lang.constant.ClassDesc;
import java.util.Map;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

// fixme... should be a nonpure extension to the list language ?
public final class VarValue<A> implements ValueTerm<A>, Comparable<VarValue<?>> {
    private final Type<A> type;
    private final Id<A> variable;

    public VarValue(Type<A> type) {
        this(type, new Id<>());
    }

    private VarValue(Type<A> type, Id<A> variable) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(variable);
        this.type = type;
        this.variable = variable;
    }

    @Override
    public void jit(ClassDesc thisClass, ClassVisitor classVisitor, MethodVisitor methodVisitor, Map<VarValue<?>, VarData> varDataMap) {
        var data = varDataMap.get(this);
        var clazz = type.erase();
        var ii = data.argument();
        if (clazz.isPrimitive()) {
            switch (clazz.getName()) {
                case "boolean", "byte", "char", "short", "int" -> {
                    methodVisitor.visitVarInsn(ILOAD, ii);
                }
                case "long" -> {
                    methodVisitor.visitVarInsn(LLOAD, ii);
                }
                case "float" -> {
                    methodVisitor.visitVarInsn(FLOAD, ii);
                }
                case "double" -> {
                    methodVisitor.visitVarInsn(DLOAD, ii);
                }
                default -> throw new IllegalStateException(clazz.getName());
            }
        } else {
            methodVisitor.visitVarInsn(ALOAD, ii);
        }
    }

    @Override
    public Term<A> visitChildren(Visitor visitor) {
        return new VarValue<>(visitor.type(type), variable);
    }

    @Override
    public String toString() {
        return "v" + variable;
    }

    public <X> Term<X> substituteIn(Term<X> root, Term<A> replacement) {
        return root.visit(new Visitor() {
            @Override
            public <T> Term<T> term(Term<T> term) {
                if (!(term instanceof VarValue<T> varValue)) {
                    return term.visitChildren(this);
                }

                if (varValue.variable == variable) {
                    return (Term) replacement;
                }
                return varValue;
            }
        });
    }

    @Override
    public int compareTo(VarValue<?> o) {
        return variable.compareTo(o.variable);
    }

    @Override
    public Type<A> type() throws TypeCheckException {
        return type;
    }
}

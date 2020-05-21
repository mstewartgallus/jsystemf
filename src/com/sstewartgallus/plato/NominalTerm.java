package com.sstewartgallus.plato;

import com.sstewartgallus.ext.variables.VarValue;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.lang.constant.ClassDesc;
import java.util.Map;
import java.util.Objects;

public final class NominalTerm<A> implements ValueTerm<A> {
    private final Type<A> type;
    private final TermTag<A> tag;

    private NominalTerm(TermTag<A> tag, Type<A> type) {
        Objects.requireNonNull(tag);
        Objects.requireNonNull(type);
        this.type = type;
        this.tag = tag;
    }

    public static <A> NominalTerm<A> ofTag(TermTag<A> tag, Type<A> type) {
        return new NominalTerm<>(tag, type);
    }

    public TermTag<A> tag() {
        return tag;
    }

    @Override
    public final Term<A> visitChildren(Visitor visitor) {
        return ofTag(tag, visitor.type(type));
    }

    @Override
    public void jit(ClassDesc thisClass, ClassVisitor classVisitor, MethodVisitor mw, Map<VarValue<?>, VarData> varDataMap) {
        // fixme... pretty sure this is wrong...
        tag.jit(thisClass, classVisitor, mw, varDataMap);
    }

    @Override
    public final Type<A> type() {
        return type;
    }

    @Override
    public String toString() {
        return tag.toString();
    }
}

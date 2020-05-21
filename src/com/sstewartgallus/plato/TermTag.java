package com.sstewartgallus.plato;

import com.sstewartgallus.ext.variables.VarValue;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.lang.constant.ClassDesc;
import java.lang.constant.Constable;
import java.util.Map;

public interface TermTag<A> extends Constable {
    default void jit(ClassDesc thisClass, ClassVisitor classVisitor, MethodVisitor methodVisitor, Map<VarValue<?>, Term.VarData> varDataMap) {
        throw null;
    }
}
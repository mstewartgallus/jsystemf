package com.sstewartgallus.plato;

import java.lang.constant.*;

public class TypeDesc<A> extends DynamicConstantDesc<Type<A>> {
    private static final ClassDesc CD_Type = ClassDesc.of("com.sstewartgallus.plato", "Type");
    private static final ClassDesc CD_NominalType = ClassDesc.of("com.sstewartgallus.plato", "NominalType");
    private static final ClassDesc CD_TypeTag = ClassDesc.of("com.sstewartgallus.plato", "TypeTag");
    private static final ClassDesc CD_FunctionTag = ClassDesc.of("com.sstewartgallus.plato", "FunctionTag");
    private static final ClassDesc CD_JavaTag = ClassDesc.of("com.sstewartgallus.ext.java", "JavaTag");

    private static final DirectMethodHandleDesc FUNCTION_MH;
    private static final DirectMethodHandleDesc NOMINAL_MH;
    private static final DirectMethodHandleDesc JAVACLASS_MH;
    private static final DirectMethodHandleDesc APPLY_MH;

    static {
        var mt = MethodTypeDesc.of(CD_FunctionTag);
        FUNCTION_MH = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, CD_FunctionTag, "function", mt);
    }

    static {
        var mt = MethodTypeDesc.of(CD_NominalType, CD_TypeTag);
        NOMINAL_MH = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, CD_NominalType, "ofTag", mt);
    }

    static {
        var mt = MethodTypeDesc.of(CD_Type, CD_Type, CD_Type);
        APPLY_MH = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.INTERFACE_STATIC, CD_Type, "apply", mt);
    }

    static {
        JAVACLASS_MH = MethodHandleDesc.ofConstructor(CD_JavaTag, ConstantDescs.CD_Class);
    }

    protected TypeDesc(DirectMethodHandleDesc bootstrapMethod, String constantName, ClassDesc constantType, ConstantDesc... bootstrapArgs) {
        super(bootstrapMethod, constantName, constantType, bootstrapArgs);
    }

    public static ConstantDesc ofFunction() {
        return DynamicConstantDesc.of(ConstantDescs.BSM_INVOKE, FUNCTION_MH);
    }

    public static ConstantDesc ofJavaClass(ClassDesc classDesc) {
        return DynamicConstantDesc.of(ConstantDescs.BSM_INVOKE, JAVACLASS_MH, classDesc);
    }

    public static <A, B> TypeDesc<B> ofApply(TypeDesc<V<A, B>> f, TypeDesc<A> x) {
        return new TypeDesc<>(ConstantDescs.BSM_INVOKE, ConstantDescs.DEFAULT_NAME, CD_Type, APPLY_MH, f, x);
    }

    public static <A> TypeDesc<A> ofNominal(ConstantDesc tag) {
        return new TypeDesc<>(ConstantDescs.BSM_INVOKE, ConstantDescs.DEFAULT_NAME, CD_Type, NOMINAL_MH, tag);
    }
}

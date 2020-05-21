package com.sstewartgallus.runtime;

import com.sstewartgallus.plato.F;
import com.sstewartgallus.plato.Type;

import java.lang.constant.*;

public class TypeDesc<A> extends DynamicConstantDesc<Type<A>> {


    private static final ClassDesc CD_Type = ClassDesc.of("com.sstewartgallus.plato", "Type");
    private static final ClassDesc CD_JavaType = ClassDesc.of("com.sstewartgallus.ext.java", "JavaType");
    private static final DirectMethodHandleDesc FUNCTION_MH;
    private static final DirectMethodHandleDesc JAVACLASS_MH;

    static {
        var mt = MethodTypeDesc.of(CD_Type, CD_Type);
        FUNCTION_MH = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.INTERFACE_VIRTUAL, CD_Type, "to", mt);
    }

    static {
        JAVACLASS_MH = MethodHandleDesc.ofConstructor(CD_JavaType, ConstantDescs.CD_Class);
    }

    protected TypeDesc(DirectMethodHandleDesc bootstrapMethod, String constantName, ClassDesc constantType, ConstantDesc... bootstrapArgs) {
        super(bootstrapMethod, constantName, constantType, bootstrapArgs);
    }

    public static <A, B> TypeDesc<F<A, B>> ofFunction(TypeDesc<A> domain, TypeDesc<B> range) {
        return new TypeDesc<>(ConstantDescs.BSM_INVOKE, ConstantDescs.DEFAULT_NAME, CD_Type, FUNCTION_MH, domain, range);
    }

    public static TypeDesc<?> ofJavaClass(ClassDesc classDesc) {
        return new TypeDesc<>(ConstantDescs.BSM_INVOKE, classDesc.displayName(), CD_Type, JAVACLASS_MH, classDesc);
    }
}

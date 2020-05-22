package com.sstewartgallus.runtime;

import com.sstewartgallus.plato.F;
import com.sstewartgallus.plato.Term;

import java.lang.constant.*;

public final class TermDesc<A> extends DynamicConstantDesc<Term<A>> {
    private static final DirectMethodHandleDesc OF_METHOD;
    private static final ClassDesc CD_Term = ClassDesc.of("com.sstewartgallus.plato", "Term");
    private static final ClassDesc CD_Type = ClassDesc.of("com.sstewartgallus.plato", "Type");
    private static final ClassDesc CD_TermBootstraps = ClassDesc.of("com.sstewartgallus.runtime", "TermBootstraps");

    static {
        var mt = MethodTypeDesc.of(CD_Term,
                ConstantDescs.CD_MethodHandles_Lookup, ConstantDescs.CD_String, ConstantDescs.CD_Class,
                CD_Type, CD_Type, ConstantDescs.CD_MethodHandle);
        OF_METHOD = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, CD_TermBootstraps, "ofMethod", mt);
    }

    protected TermDesc(DirectMethodHandleDesc bootstrapMethod, String constantName, ClassDesc constantType, ConstantDesc... bootstrapArgs) {
        super(bootstrapMethod, constantName, constantType, bootstrapArgs);
    }

    public static <A, B> TermDesc<F<A, B>> ofMethod(TypeDesc<A> domain, TypeDesc<B> range, DirectMethodHandleDesc methodHandleDesc) {
        return new TermDesc<>(OF_METHOD, methodHandleDesc.methodName(), CD_Term, domain, range, methodHandleDesc);
    }
}

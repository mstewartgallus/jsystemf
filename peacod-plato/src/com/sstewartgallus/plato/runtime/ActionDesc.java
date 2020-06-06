package com.sstewartgallus.plato.runtime;

import com.sstewartgallus.plato.syntax.type.TypeDesc;

import java.lang.constant.*;

public final class ActionDesc<A> extends DynamicConstantDesc<U> {
    private static final DirectMethodHandleDesc OF_METHOD;

    private static final ClassDesc CD_Action = ClassDesc.of("com.sstewartgallus.plato.runtime", "Action");
    private static final ClassDesc CD_ActionBootstraps = ClassDesc.of("com.sstewartgallus.plato.runtime", "ActionBootstraps");

    private static final ClassDesc CD_Type = ClassDesc.of("com.sstewartgallus.plato.syntax.type", "Type");

    static {
        var mt = MethodTypeDesc.of(CD_Action,
                ConstantDescs.CD_MethodHandles_Lookup, ConstantDescs.CD_String, ConstantDescs.CD_Class,
                CD_Type, ConstantDescs.CD_MethodHandle);
        OF_METHOD = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, CD_ActionBootstraps, "ofMethod", mt);
    }

    protected ActionDesc(DirectMethodHandleDesc bootstrapMethod, String constantName, ClassDesc constantType, ConstantDesc... bootstrapArgs) {
        super(bootstrapMethod, constantName, constantType, bootstrapArgs);
    }

    /// fixme....
    public static <A> ActionDesc<A> ofMethod(TypeDesc<A> type, DirectMethodHandleDesc methodHandleDesc) {
        return new ActionDesc<>(OF_METHOD, methodHandleDesc.methodName(), CD_Action, type, methodHandleDesc);
    }
}

package com.sstewartgallus.plato.runtime;

import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.internal.AsmUtils;

import java.lang.constant.*;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static java.lang.invoke.MethodType.methodType;

public final class ActionDesc<A> extends DynamicConstantDesc<U> {
    private static final DirectMethodHandleDesc OF_METHOD;
    private static final DirectMethodHandleDesc OF_REFERENCE;
    private static final DirectMethodHandleDesc OF_CLOSURE;
    private static final DirectMethodHandleDesc INVOKE;

    private static final ClassDesc CD_Type = TypeDesc.CD_Type;
    private static final String PACKAGE = ActionDesc.class.getPackageName();
    private static final ClassDesc CD_U = ClassDesc.of(PACKAGE, "U");
    private static final ClassDesc CD_ActionBootstraps = ClassDesc.of(PACKAGE, "ActionBootstraps");

    static {
        var mt = MethodTypeDesc.of(CD_U,
                ConstantDescs.CD_MethodHandles_Lookup, ConstantDescs.CD_String, ConstantDescs.CD_Class,
                CD_Type, ConstantDescs.CD_MethodHandle);
        OF_METHOD = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, CD_ActionBootstraps, "ofMethod", mt);
    }

    static {
        var mt = MethodTypeDesc.of(ConstantDescs.CD_CallSite,
                ConstantDescs.CD_MethodHandles_Lookup, ConstantDescs.CD_String, ConstantDescs.CD_MethodType,
                CD_Type, ConstantDescs.CD_MethodHandle);
        OF_CLOSURE = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, CD_ActionBootstraps, "closureFactory", mt);
    }

    static {
        var mt = MethodTypeDesc.of(CD_U,
                ConstantDescs.CD_MethodHandles_Lookup, ConstantDescs.CD_String, ConstantDescs.CD_Class,
                ConstantDescs.CD_String, CD_Type);
        OF_REFERENCE = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, CD_ActionBootstraps, "ofReference", mt);
    }

    static {
        var mt = MethodTypeDesc.ofDescriptor(methodType(CallSite.class, MethodHandles.Lookup.class, String.class, MethodType.class, MethodType.class).descriptorString());
        INVOKE = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, AsmUtils.CD_ActionBootstraps, "invoke", mt);
    }

    protected ActionDesc(DirectMethodHandleDesc bootstrapMethod, String constantName, ClassDesc constantType, ConstantDesc... bootstrapArgs) {
        super(bootstrapMethod, constantName, constantType, bootstrapArgs);
    }

    public static <A> ActionDesc<A> ofMethod(TypeDesc<A> type, DirectMethodHandleDesc methodHandleDesc) {
        return new ActionDesc<>(OF_METHOD, methodHandleDesc.methodName(), CD_U, type, methodHandleDesc);
    }

    public static DynamicCallSiteDesc ofClosure(TypeDesc<?> type, DirectMethodHandleDesc methodHandleDesc, ClassDesc... environment) {
        var methodType = MethodTypeDesc.of(CD_U, environment);
        return DynamicCallSiteDesc.of(OF_CLOSURE, methodHandleDesc.methodName(), methodType, type, methodHandleDesc);
    }

    public static <A> ActionDesc<A> ofReference(String packageName, String canonicalName, TypeDesc<A> typeDesc) {
        return new ActionDesc<>(OF_REFERENCE, canonicalName, CD_U, packageName, typeDesc);
    }

    public static DynamicCallSiteDesc getApplyLabel(MethodTypeDesc desc) {
        var methodTypeDesc = methodType(Label.class, U.class).describeConstable().get();
        return DynamicCallSiteDesc.of(INVOKE, "APPLY", methodTypeDesc, desc);
    }

    public static DynamicCallSiteDesc callApplyLabel(MethodTypeDesc methodType) {
        methodType = methodType.insertParameterTypes(0, Label.class.describeConstable().get(), U.class.describeConstable().get());
        return DynamicCallSiteDesc.of(INVOKE, "CALL", methodType, methodType);
    }
}

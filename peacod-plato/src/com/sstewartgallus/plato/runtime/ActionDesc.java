package com.sstewartgallus.plato.runtime;

import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.type.U;

import java.lang.constant.*;

public final class ActionDesc<A> extends DynamicConstantDesc<U> {
    private static final DirectMethodHandleDesc CALL_BSM;

    private static final ClassDesc CD_Type = TypeDesc.CD_Type;
    private static final String PACKAGE = ActionDesc.class.getPackageName();
    private static final ClassDesc CD_U = ClassDesc.of(PACKAGE, "U");
    private static final ClassDesc CD_ActionBootstraps = ClassDesc.of(PACKAGE, "ActionBootstraps");

    static {
        var mt = MethodTypeDesc.of(ConstantDescs.CD_CallSite,
                ConstantDescs.CD_MethodHandles_Lookup, ConstantDescs.CD_String, ConstantDescs.CD_MethodType,
                ConstantDescs.CD_String, ConstantDescs.CD_String);
        CALL_BSM = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, CD_ActionBootstraps, "bootstrap", mt);
    }

    protected ActionDesc(DirectMethodHandleDesc bootstrapMethod, String constantName, ClassDesc constantType, ConstantDesc... bootstrapArgs) {
        super(bootstrapMethod, constantName, constantType, bootstrapArgs);
    }

    // fixme... pass in type ?
    public static DynamicCallSiteDesc callGlobal(String packageName, String name, MethodTypeDesc methodType) {
        return DynamicCallSiteDesc.of(CALL_BSM, "CALL", methodType, packageName, name);
    }
}

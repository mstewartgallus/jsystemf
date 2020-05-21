package com.sstewartgallus.plato;

import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicConstantDesc;

public class AsmUtils {
    private static Handle toAsm(DirectMethodHandleDesc desc) {
        return new Handle(desc.refKind(), toAsm(desc.owner()), desc.methodName(), desc.lookupDescriptor(), desc.isOwnerInterface());
    }

    private static String toAsm(ClassDesc owner) {
        return (owner.packageName() + "." + owner.displayName()).replace('.', '/');
    }

    static Object toAsm(ConstantDesc desc) {
        if (desc instanceof Integer || desc instanceof Float || desc instanceof String) {
            return desc;
        }

        if (desc instanceof DynamicConstantDesc<?> dynamic) {
            return new ConstantDynamic(dynamic.constantName(), dynamic.constantType().descriptorString(), toAsm(dynamic.bootstrapMethod()),
                    dynamic.bootstrapArgsList().stream().map(AsmUtils::toAsm).toArray());
        }
        if (desc instanceof DirectMethodHandleDesc direct) {
            return new Handle(direct.refKind(), toAsm(direct.owner()), direct.methodName(), direct.lookupDescriptor(), direct.isOwnerInterface());
        }

        throw new UnsupportedOperationException(desc.toString());
    }
}

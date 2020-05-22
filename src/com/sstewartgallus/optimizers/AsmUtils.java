package com.sstewartgallus.optimizers;

import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;

import java.lang.constant.*;

public class AsmUtils {

    public static final ClassDesc CD_TermBootstraps = ClassDesc.of("com.sstewartgallus.runtime", "TermBootstraps");

    public static Handle toHandle(MethodHandleDesc desc) {
        if (desc instanceof DirectMethodHandleDesc direct) {
            return toHandle(direct);
        }
        throw new UnsupportedOperationException(desc.toString());
    }

    public static Handle toHandle(DirectMethodHandleDesc desc) {
        return new Handle(desc.refKind(), toAsm(desc.owner()), desc.methodName(), desc.lookupDescriptor(), desc.isOwnerInterface());
    }

    private static String toAsm(ClassDesc owner) {
        return (owner.packageName() + "." + owner.displayName()).replace('.', '/');
    }

    public static Object toAsm(ConstantDesc desc) {
        if (desc instanceof String) {
            throw new Error(desc.toString());
        }
        if (desc instanceof Integer || desc instanceof Float) {
            return desc;
        }

        if (desc instanceof DynamicConstantDesc<?> dynamic) {
            return new ConstantDynamic(dynamic.constantName(), dynamic.constantType().descriptorString(), toHandle(dynamic.bootstrapMethod()),
                    dynamic.bootstrapArgsList().stream().map(AsmUtils::toAsm).toArray());
        }
        if (desc instanceof DirectMethodHandleDesc direct) {
            return new Handle(direct.refKind(), toAsm(direct.owner()), direct.methodName(), direct.lookupDescriptor(), direct.isOwnerInterface());
        }

        if (desc instanceof ClassDesc classDesc) {
            return org.objectweb.asm.Type.getType(classDesc.descriptorString());
        }
        throw new UnsupportedOperationException(desc.toString());
    }
}

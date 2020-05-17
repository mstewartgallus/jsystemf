package com.sstewartgallus.runtime;

import com.sstewartgallus.plato.LambdaValue;
import com.sstewartgallus.plato.Term;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.TypeBasedGuardingDynamicLinker;
import jdk.dynalink.linker.support.Guards;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodHandles.lookup;

public final class LambdaLinker implements TypeBasedGuardingDynamicLinker {

    // fixme... can we have a better normalize than just abstract dispatch?
    private static final MethodHandle APPLY_MH;

    static {
        try {
            APPLY_MH = lookup().findVirtual(LambdaValue.class, "apply", MethodType.methodType(Term.class, Term.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean canLinkType(Class<?> aClass) {
        return LambdaValue.class.isAssignableFrom(aClass);
    }

    @Override
    public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices) {
        var receiver = (LambdaValue<?, ?>) linkRequest.getReceiver();

        var methodType = linkRequest.getCallSiteDescriptor().getMethodType();
        if (methodType.parameterCount() != 3) {
            return null;
        }

        var mh = APPLY_MH;
        mh = dropArguments(mh, 1, Void.class);
        return new GuardedInvocation(mh, Guards.isOfClass(LambdaValue.class, methodType));
    }
}

package com.sstewartgallus.ext.mh;

import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.TypeBasedGuardingDynamicLinker;
import jdk.dynalink.linker.support.Guards;

import static java.lang.invoke.MethodHandles.dropArguments;

public final class JitLinker implements TypeBasedGuardingDynamicLinker {
    @Override
    public boolean canLinkType(Class<?> aClass) {
        return JitValue.class.isAssignableFrom(aClass);
    }

    @Override
    public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices) {
        var receiver = (JitValue<?, ?, ?>) linkRequest.getReceiver();

        var cs = linkRequest.getCallSiteDescriptor();
        var methodType = cs.getMethodType();

        var handle = receiver.methodHandle();
        handle = dropArguments(handle, 1, Void.class);
        return new GuardedInvocation(handle, Guards.isOfClass(JitValue.class, methodType));
    }
}

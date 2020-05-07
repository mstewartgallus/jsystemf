package com.sstewartgallus.runtime;

import jdk.dynalink.*;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.TypeBasedGuardingDynamicLinker;
import jdk.dynalink.support.SimpleRelinkableCallSite;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public final class ValueLinker implements TypeBasedGuardingDynamicLinker {
    private static final DynamicLinker DYNAMIC_LINKER;

    static {
        var factory = new DynamicLinkerFactory();
        factory.setPrioritizedLinker(new ValueLinker());
        // fixme... consider a prelink transformer for function evals?
        factory.setSyncOnRelink(true);
        DYNAMIC_LINKER = factory.createLinker();
    }
    // fixme.. look more into https://gitlab.haskell.org/ghc/ghc/-/wikis/commentary/rts/haskell-execution/function-calls

    public static CallSite link(MethodHandles.Lookup lookup, Operation operation, MethodType methodType) {
        return DYNAMIC_LINKER.link(
                new SimpleRelinkableCallSite(
                        new CallSiteDescriptor(lookup, operation, methodType)));
    }

    @Override
    public boolean canLinkType(Class<?> aClass) {
        return Value.class.isAssignableFrom(aClass);
    }

    @Override
    public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices) throws NoSuchFieldException, IllegalAccessException {
        // fixme... do stuff for other values?
        var receiver = (Closure<?>) linkRequest.getReceiver();
        return receiver.getGuardedInvocation(linkRequest, linkerServices);
    }
}

package com.sstewartgallus.runtime;

import com.sstewartgallus.plato.ThunkTerm;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.TypeBasedGuardingDynamicLinker;

public final class ThunkLinker implements TypeBasedGuardingDynamicLinker {
    @Override
    public boolean canLinkType(Class<?> aClass) {
        return ThunkTerm.class.isAssignableFrom(aClass);
    }

    @Override
    public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices) throws Exception {
        var receiver = (ThunkTerm<?>) linkRequest.getReceiver();

        System.err.println("fixme.. move away from thunks");

        throw null;
    }
}

package com.sstewartgallus.plato.runtime;

import com.sstewartgallus.plato.runtime.internal.JavaLinker;
import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.DynamicLinker;
import jdk.dynalink.DynamicLinkerFactory;
import jdk.dynalink.Operation;
import jdk.dynalink.support.SimpleRelinkableCallSite;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;

final class ActionLinker {
    private static final DynamicLinker DYNAMIC_LINKER;

    static {
        var linkers = List.of(new JitLinker(), new JavaLinker(), new FunLinker());
        var factory = new DynamicLinkerFactory();
        factory.setPrioritizedLinkers(linkers);
        factory.setSyncOnRelink(true);
        DYNAMIC_LINKER = factory.createLinker();
    }
    
    public static CallSite link(MethodHandles.Lookup lookup, Operation operation, MethodType methodType) {
        return DYNAMIC_LINKER.link(
                new SimpleRelinkableCallSite(
                        new CallSiteDescriptor(lookup, operation, methodType)));
    }
}

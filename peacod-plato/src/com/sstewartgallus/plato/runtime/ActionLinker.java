package com.sstewartgallus.plato.runtime;

import com.sstewartgallus.plato.java.IntF;
import com.sstewartgallus.plato.runtime.type.U;
import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.DynamicLinker;
import jdk.dynalink.DynamicLinkerFactory;
import jdk.dynalink.Operation;
import jdk.dynalink.linker.*;
import jdk.dynalink.support.SimpleRelinkableCallSite;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.function.Supplier;

import static java.lang.invoke.MethodHandles.Lookup;
import static java.lang.invoke.MethodHandles.lookup;

final class ActionLinker implements TypeBasedGuardingDynamicLinker, GuardingTypeConverterFactory {
    private static final DynamicLinker DYNAMIC_LINKER;
    private static final MethodHandle INT_ACTION_CON_MH;
    private static final MethodHandle INT_ACTION_VALUE_MH;

    static {
        var linkers = List.of(new ActionLinker());
        var factory = new DynamicLinkerFactory();
        factory.setPrioritizedLinkers(linkers);
        factory.setSyncOnRelink(true);
        DYNAMIC_LINKER = factory.createLinker();
    }


    static {
        try {
            INT_ACTION_CON_MH = lookup().findStatic(IntF.class, "of", MethodType.methodType(IntF.class, int.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new Error(e);
        }
    }

    static {
        try {
            INT_ACTION_VALUE_MH = lookup().findVirtual(IntF.class, "value", MethodType.methodType(int.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new Error(e);
        }
    }

    public static CallSite link(MethodHandles.Lookup lookup, Operation operation, MethodType methodType) {
        return DYNAMIC_LINKER.link(
                new SimpleRelinkableCallSite(
                        new CallSiteDescriptor(lookup, operation, methodType)));
    }

    @Override
    public boolean canLinkType(Class<?> aClass) {
        return U.class.isAssignableFrom(aClass);
    }

    @Override
    public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices) {
        var receiver = linkRequest.getReceiver();

        return null;
    }


    @Override
    public GuardedInvocation convertToType(Class<?> sourceType, Class<?> targetType, Supplier<Lookup> lookupSupplier) throws Exception {
        if ((sourceType == int.class || sourceType == Integer.class) && (targetType == U.class || targetType == F.class || targetType == IntF.class)) {
            return new GuardedInvocation(INT_ACTION_CON_MH.asType(MethodType.methodType(targetType, sourceType)));
        }
        return null;
    }
}

package com.sstewartgallus.plato.runtime.internal;

import com.sstewartgallus.plato.java.IntAction;
import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.U;
import jdk.dynalink.linker.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import static java.lang.invoke.MethodHandles.Lookup;
import static java.lang.invoke.MethodHandles.lookup;

public final class JavaLinker implements TypeBasedGuardingDynamicLinker, GuardingTypeConverterFactory {
    private static final MethodHandle INT_ACTION_CON_MH;
    private static final MethodHandle INT_ACTION_VALUE_MH;

    static {
        try {
            INT_ACTION_CON_MH = lookup().findConstructor(IntAction.class, MethodType.methodType(void.class, int.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new Error(e);
        }
    }

    static {
        try {
            INT_ACTION_VALUE_MH = lookup().findVirtual(IntAction.class, "value", MethodType.methodType(int.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new Error(e);
        }
    }

    @Override
    public boolean canLinkType(Class<?> aClass) {
        return IntAction.class.isAssignableFrom(aClass);
    }

    @Override
    public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices) throws Exception {
        return null;
    }

    @Override
    public GuardedInvocation convertToType(Class<?> sourceType, Class<?> targetType, Supplier<Lookup> lookupSupplier) throws Exception {
        System.err.println(sourceType + " convert to " + targetType);
        if ((sourceType == int.class || sourceType == Integer.class) && (targetType == U.class || targetType == F.class || targetType == IntAction.class)) {
            return new GuardedInvocation(INT_ACTION_CON_MH.asType(MethodType.methodType(targetType, sourceType)));
        }
        if ((sourceType == U.class || sourceType == F.class || sourceType == IntAction.class) && (targetType == int.class  || targetType == Integer.class)) {
            return new GuardedInvocation(INT_ACTION_VALUE_MH.asType(MethodType.methodType(targetType, sourceType)));
        }
        return null;
    }
}


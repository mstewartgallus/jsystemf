package com.sstewartgallus.plato.runtime;

import com.sstewartgallus.plato.ir.type.Type;
import com.sstewartgallus.plato.java.IntF;
import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.Guards;
import jdk.dynalink.support.SimpleRelinkableCallSite;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.function.Supplier;

import static java.lang.invoke.MethodHandles.*;

final class ActionLinker implements TypeBasedGuardingDynamicLinker, GuardingTypeConverterFactory {
    private static final DynamicLinker DYNAMIC_LINKER;
    private static final MethodHandle APPLY_MH;
    private static final MethodHandle APPLY_V_MH;
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
            APPLY_MH = lookup().findVirtual(FnImpl.class, "applyGeneric", MethodType.methodType(U.class, Object.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        try {
            APPLY_V_MH = lookup().findVirtual(VImpl.class, "applyType", MethodType.methodType(U.class, Type.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
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

    private static GuardedInvocation getJitInvocation(LinkRequest linkRequest, LinkerServices linkerServices, Object receiver, JitStatic<?> jitValueReceiver) {
        // fixme.. how to avoid all the currying and such...
        var mh = jitValueReceiver.methodHandle();

        var cs = linkRequest.getCallSiteDescriptor();

        var op = cs.getOperation();
        var name = (NamedOperation) op;
        var typeToAdaptTo = (MethodType) name.getName();
        mh = dropArguments(mh, 0, Label.class);

        return new GuardedInvocation(
                dropArguments(constant(Label.class, new Label<>(adaptTo(mh, typeToAdaptTo, linkRequest))), 0, JitStatic.class),
                Guards.getIdentityGuard(jitValueReceiver));
    }

    private static GuardedInvocation getVInvocation(LinkRequest linkRequest, LinkerServices linkerServices, VImpl<?, ?> vImpl) {
        var cs = linkRequest.getCallSiteDescriptor();
        var methodType = cs.getMethodType();

        var op = cs.getOperation();
        var name = (NamedOperation) op;
        var typeToAdaptTo = (MethodType) name.getName();

        return new GuardedInvocation(
                dropArguments(constant(Label.class, new Label<>(adaptTo(APPLY_V_MH, typeToAdaptTo, linkRequest))), 0, VImpl.class),
                Guards.isOfClass(VImpl.class, methodType));
    }

    private static GuardedInvocation getFnInvocation(LinkRequest linkRequest, LinkerServices linkerServices, FnImpl<?, ?> fnReceiver) {
        var cs = linkRequest.getCallSiteDescriptor();
        var methodType = cs.getMethodType();

        var op = cs.getOperation();
        var name = (NamedOperation) op;
        var typeToAdaptTo = (MethodType) name.getName();

        return new GuardedInvocation(
                dropArguments(constant(Label.class, new Label<>(adaptTo(APPLY_MH, typeToAdaptTo, linkRequest))), 0, FnImpl.class),
                Guards.isOfClass(FnImpl.class, methodType));
    }

    private static MethodHandle adaptTo(MethodHandle methodHandle, MethodType methodType,
                                        LinkRequest linkRequest) {
        var paramCount = methodType.parameterCount();

        var myCount = methodHandle.type().parameterCount() - 1;
        if (paramCount == myCount) {
            return methodHandle;
        }

        if (paramCount > myCount) {
            var newMethodType = methodType;
            var theRest = newMethodType.dropParameterTypes(0, methodHandle.type().parameterCount() - 1);

            var cs = linkRequest.getCallSiteDescriptor();

            var getLabel = ActionLinker.link(cs.getLookup(),
                    StandardOperation.GET.withNamespace(StandardNamespace.METHOD).named(theRest),
                    MethodType.methodType(Label.class, U.class)).dynamicInvoker();

            newMethodType = theRest.insertParameterTypes(0, Label.class, U.class);

            var callLabel = ActionLinker.link(cs.getLookup(), StandardOperation.CALL, newMethodType).dynamicInvoker();

            callLabel = filterArguments(callLabel, 0, getLabel);
            var permutes = new int[callLabel.type().parameterCount()];
            for (var ii = 0; ii < permutes.length; ++ii) {
                permutes[ii] = ii - 1;
            }
            permutes[0] = 0;

            callLabel = permuteArguments(callLabel, theRest.insertParameterTypes(0, U.class), permutes);

            var mh = methodHandle;
            callLabel = dropArguments(callLabel, 1, mh.type().parameterList());
            mh = dropArguments(mh, mh.type().parameterCount(), theRest.parameterList());
            mh = foldArguments(callLabel, mh);

            return mh;
        }
        // fixme... spin a closure
        throw null;
    }

    private static GuardedInvocation getIntFInvocation(LinkRequest linkRequest, LinkerServices linkerServices, IntF fReceiver) {
        var cs = linkRequest.getCallSiteDescriptor();
        var methodType = cs.getMethodType();

        var op = cs.getOperation();
        var name = (NamedOperation) op;
        var typeToAdaptTo = (MethodType) name.getName();

        return new GuardedInvocation(
                dropArguments(constant(Label.class, new Label<>(adaptTo(INT_ACTION_VALUE_MH, typeToAdaptTo, linkRequest))), 0, IntF.class),
                Guards.isOfClass(IntF.class, methodType));
    }

    @Override
    public boolean canLinkType(Class<?> aClass) {
        return U.class.isAssignableFrom(aClass) || Label.class.isAssignableFrom(aClass);
    }

    @Override
    public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices) {
        var receiver = linkRequest.getReceiver();

        if (receiver instanceof Label<?> labelReceiver) {
            return getLabelInvocation(linkRequest, linkerServices, labelReceiver);
        }
        if (receiver instanceof FnImpl<?, ?> fnReceiver) {
            return getFnInvocation(linkRequest, linkerServices, fnReceiver);
        }
        if (receiver instanceof VImpl<?, ?> vImpl) {
            return getVInvocation(linkRequest, linkerServices, vImpl);
        }
        if (receiver instanceof IntF fReceiver) {
            return getIntFInvocation(linkRequest, linkerServices, fReceiver);
        }
        if (receiver instanceof JitStatic<?> jitValueReceiver) {
            return getJitInvocation(linkRequest, linkerServices, receiver, jitValueReceiver);
        }

        return null;
    }

    private GuardedInvocation getLabelInvocation(LinkRequest linkRequest, LinkerServices linkerServices, Label<?> label) {
        var cs = linkRequest.getCallSiteDescriptor();
        var methodType = cs.getMethodType();

        var mh = label.handle();

        mh = dropArguments(mh, 0, Label.class);

        return new GuardedInvocation(
                linkerServices.asType(mh, methodType),
                Guards.getIdentityGuard(label));
    }

    @Override
    public GuardedInvocation convertToType(Class<?> sourceType, Class<?> targetType, Supplier<Lookup> lookupSupplier) throws Exception {
        if ((sourceType == int.class || sourceType == Integer.class) && (targetType == U.class || targetType == F.class || targetType == IntF.class)) {
            return new GuardedInvocation(INT_ACTION_CON_MH.asType(MethodType.methodType(targetType, sourceType)));
        }
        if ((sourceType == U.class || sourceType == F.class || sourceType == IntF.class) && (targetType == int.class || targetType == Integer.class)) {
            return new GuardedInvocation(INT_ACTION_VALUE_MH.asType(MethodType.methodType(targetType, sourceType)));
        }
        return null;
    }
}

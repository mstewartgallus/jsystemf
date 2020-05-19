package com.sstewartgallus.runtime;

import com.sstewartgallus.plato.SimpleTypeLambdaValue;
import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.Type;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.TypeBasedGuardingDynamicLinker;
import jdk.dynalink.linker.support.Guards;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.Arrays;

import static java.lang.invoke.MethodHandles.*;

public final class TypeLambdaLinker implements TypeBasedGuardingDynamicLinker {

    // fixme... can we have a better normalize than just abstract dispatch?
    private static final MethodHandle APPLY_MH;

    static {
        try {
            APPLY_MH = lookup().findVirtual(SimpleTypeLambdaValue.class, "apply", MethodType.methodType(Term.class, Type.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean canLinkType(Class<?> aClass) {
        return SimpleTypeLambdaValue.class.isAssignableFrom(aClass);
    }

    @Override
    public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices) {
        var receiver = (SimpleTypeLambdaValue<?, ?>) linkRequest.getReceiver();
        var cs = linkRequest.getCallSiteDescriptor();
        var methodType = cs.getMethodType();

        System.err.println("linking " + Arrays.toString(linkRequest.getArguments()));

        var mh = APPLY_MH;
        mh = dropArguments(mh, 1, Void.class);

        var parameterCount = methodType.parameterCount();
        if (parameterCount > 3) {
            mh = linkerServices.asType(mh, methodType.dropParameterTypes(3, parameterCount).changeReturnType(Term.class));

            var restTypes = methodType
                    .dropParameterTypes(0, 3)
                    .insertParameterTypes(0, Term.class, Void.class);
            var handleTheRest = TermLinker
                    .link(cs.getLookup(), cs.getOperation(), restTypes)
                    .dynamicInvoker();

            handleTheRest = insertArguments(handleTheRest, 1, (Object) null);
            handleTheRest = dropArguments(handleTheRest, 1, mh.type().parameterList());

            mh = foldArguments(handleTheRest, mh);
        }
        System.err.println("linked " + mh + " " + Arrays.toString(linkRequest.getArguments()));
        return new GuardedInvocation(mh, Guards.isOfClass(SimpleTypeLambdaValue.class, methodType));
    }
}

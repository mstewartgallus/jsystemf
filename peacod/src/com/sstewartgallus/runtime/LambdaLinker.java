package com.sstewartgallus.runtime;

import com.sstewartgallus.plato.LambdaTerm;
import com.sstewartgallus.plato.Term;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.TypeBasedGuardingDynamicLinker;
import jdk.dynalink.linker.support.Guards;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static java.lang.invoke.MethodHandles.*;

public final class LambdaLinker implements TypeBasedGuardingDynamicLinker {

    // fixme... can we have a better normalize than just abstract dispatch?
    private static final MethodHandle APPLY_MH;

    static {
        try {
            APPLY_MH = lookup().findVirtual(LambdaTerm.class, "apply", MethodType.methodType(Term.class, Term.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean canLinkType(Class<?> aClass) {
        return LambdaTerm.class.isAssignableFrom(aClass);
    }

    @Override
    public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices) throws Exception {
        var receiver = (LambdaTerm<?, ?>) linkRequest.getReceiver();
        var cs = linkRequest.getCallSiteDescriptor();
        var methodType = cs.getMethodType();

        var parameterCount = methodType.parameterCount();
        if (parameterCount <= 3) {
            var mh = APPLY_MH;
            mh = dropArguments(mh, 1, Void.class);
            return new GuardedInvocation(
                    linkerServices.asType(mh, methodType),
                    Guards.isOfClass(LambdaTerm.class, methodType));
        }

        var newMethodType = methodType;
        var theRest = newMethodType.dropParameterTypes(0, 3);
        newMethodType = theRest.insertParameterTypes(0, Term.class, Void.class);

        var mh = APPLY_MH;

        // fixme... use linker services if possible...
        var handleTheRest = TermLinker.link(cs.getLookup(), cs.getOperation(), newMethodType).dynamicInvoker();
        handleTheRest = insertArguments(handleTheRest, 1, new Object[]{null});
        handleTheRest = dropArguments(handleTheRest, 1, mh.type().parameterList());

        mh = dropArguments(mh, mh.type().parameterCount(), theRest.parameterList());
        mh = foldArguments(handleTheRest, mh);
        mh = dropArguments(mh, 1, Void.class);
        return new GuardedInvocation(
                linkerServices.asType(mh, methodType),
                Guards.isOfClass(LambdaTerm.class, methodType));
    }
}


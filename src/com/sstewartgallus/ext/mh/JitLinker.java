package com.sstewartgallus.ext.mh;

import com.sstewartgallus.plato.Term;
import com.sstewartgallus.runtime.TermLinker;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.TypeBasedGuardingDynamicLinker;
import jdk.dynalink.linker.support.Guards;

import static java.lang.invoke.MethodHandles.*;

public final class JitLinker implements TypeBasedGuardingDynamicLinker {
    @Override
    public boolean canLinkType(Class<?> aClass) {
        return JitValue.class.isAssignableFrom(aClass);
    }

    @Override
    public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices) {
        var receiver = linkRequest.getReceiver();

        if (receiver instanceof JitValue<?, ?> jitValueReceiver) {
            // fixme.. how to avoid all the currying and such...
            var handle = jitValueReceiver.methodHandle();
            var newType = handle.type();
            var cs = linkRequest.getCallSiteDescriptor();
            var methodType = cs.getMethodType();

            var parameterCount = methodType.parameterCount();
            // fixme.. cleanly handle super saturated calls, under saturated, etc....
            if (parameterCount <= handle.type().parameterCount() + 2) {
                var mh = handle;
                mh = dropArguments(mh, 0, Term.class, Void.class);
                return new GuardedInvocation(
                        linkerServices.asType(mh, methodType),
                        Guards.getIdentityGuard(receiver));
            }

            var newMethodType = methodType;
            var theRest = newMethodType.dropParameterTypes(0, handle.type().parameterCount() + 2);
            newMethodType = theRest.insertParameterTypes(0, Term.class, Void.class);

            var mh = handle;
            mh = dropArguments(mh, 0, Term.class);

            // fixme... attach properly... to the result...
            var handleTheRest = TermLinker.link(cs.getLookup(), cs.getOperation(), newMethodType).dynamicInvoker();
            handleTheRest = insertArguments(handleTheRest, 1, new Object[]{null});
            handleTheRest = dropArguments(handleTheRest, 1, mh.type().parameterList());

            mh = dropArguments(mh, mh.type().parameterCount(), theRest.parameterList());
            mh = foldArguments(handleTheRest, mh);
            mh = dropArguments(mh, 1, Void.class);
            return new GuardedInvocation(
                    linkerServices.asType(mh, methodType),
                    Guards.getIdentityGuard(receiver));
        }

        return null;
    }
}

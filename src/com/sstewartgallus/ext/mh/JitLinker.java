package com.sstewartgallus.ext.mh;

import com.sstewartgallus.plato.Term;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.TypeBasedGuardingDynamicLinker;
import jdk.dynalink.linker.support.Guards;

import java.util.Arrays;

import static java.lang.invoke.MethodHandles.dropArguments;

public final class JitLinker implements TypeBasedGuardingDynamicLinker {
    @Override
    public boolean canLinkType(Class<?> aClass) {
        return JitValue.class.isAssignableFrom(aClass);
    }

    @Override
    public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices) {
        var receiver = linkRequest.getReceiver();

        if (receiver instanceof JitLambdaValue<?, ?, ?, ?> jitValueReceiver) {
            // fixme.. how to avoid all the currying and such...
            var handle = jitValueReceiver.methodHandle();
            var newType = handle.type();
            {
                var len = newType.parameterCount();
                var newArgs = new Class[len];
                Arrays.fill(newArgs, Term.class);
                newType = newType.dropParameterTypes(0, len).appendParameterTypes(newArgs);
            }

            handle = linkerServices.asType(handle, newType);
            handle = handle.asSpreader(Term[].class, handle.type().parameterCount());

            handle = dropArguments(handle, 0, JitLambdaValue.class, Void.class);

            return new GuardedInvocation(handle, Guards.getIdentityGuard(jitValueReceiver));
        }

        return null;
    }
}

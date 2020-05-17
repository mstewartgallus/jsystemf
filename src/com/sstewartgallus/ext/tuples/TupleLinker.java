package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.plato.Term;
import com.sstewartgallus.runtime.TermLinker;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.TypeBasedGuardingDynamicLinker;
import jdk.dynalink.linker.support.Guards;

import java.util.Arrays;

import static java.lang.invoke.MethodHandles.*;

public final class TupleLinker implements TypeBasedGuardingDynamicLinker {

    @Override
    public boolean canLinkType(Class<?> aClass) {
        return CurryValue.class.isAssignableFrom(aClass) || UncurryValue.class.isAssignableFrom(aClass);
    }

    @Override
    public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices) {
        var receiver = linkRequest.getReceiver();
        var cs = linkRequest.getCallSiteDescriptor();

        var methodType = cs.getMethodType();
        if (receiver instanceof UncurryValue<?, ?, ?> uncurry) {
            // fixme... seems silly..
            System.err.println(uncurry.signature() + " " + Arrays.toString(linkRequest.getArguments()));

            var mh = TermLinker
                    .link(cs.getLookup(), cs.getOperation(), cs.getMethodType())
                    .dynamicInvoker();

            var parameterCount = methodType.parameterCount();

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
            return new GuardedInvocation(mh, Guards.isOfClass(UncurryValue.class, mh.type()));
        }
        throw null;
        //return new GuardedInvocation(mh, Guards.isOfClass(CurryValue.class));
    }
}

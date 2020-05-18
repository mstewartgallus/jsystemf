package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.plato.Term;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.TypeBasedGuardingDynamicLinker;

public final class TupleLinker implements TypeBasedGuardingDynamicLinker {
    @Override
    public boolean canLinkType(Class<?> aClass) {
        return CurriedLambdaValue.class.isAssignableFrom(aClass);
    }

    @Override
    public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices) throws Exception {
        var receiver = linkRequest.getReceiver();
        var cs = linkRequest.getCallSiteDescriptor();

        var methodType = cs.getMethodType();

        if (receiver instanceof CurriedLambdaValue<?, ?, ?> curry) {
            var f = curry.f();

            var oldArgs = linkRequest.getArguments();

            var restTypes = methodType
                    .dropParameterTypes(0, 3)
                    .insertParameterTypes(0, Term.class, Void.class);

            var arguments = new Object[restTypes.parameterCount()];
            arguments[0] = f;
            arguments[1] = null;
            System.arraycopy(oldArgs, 2, arguments, 2, arguments.length - 2);

            var newRequest = linkRequest.replaceArguments(cs.changeMethodType(methodType.changeParameterType(0, Term.class)), arguments);
            var guard = linkerServices.getGuardedInvocation(newRequest);
            if (null == guard) {
                throw null;
            }
            // fixme.. add filter to grab f...
            return guard;
        }

        throw null;
        //return new GuardedInvocation(mh, Guards.isOfClass(CurryValue.class));
    }
}

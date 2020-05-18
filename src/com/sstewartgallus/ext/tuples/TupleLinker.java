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
    private static final InvalidationException INVALIDATION_EXCEPTION = new InvalidationException();

    @Override
    public boolean canLinkType(Class<?> aClass) {
        return CurriedLambdaValue.class.isAssignableFrom(aClass) || UncurryValue.class.isAssignableFrom(aClass);
    }

    @Override
    public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices) throws Exception {
        var receiver = linkRequest.getReceiver();
        var cs = linkRequest.getCallSiteDescriptor();

        var methodType = cs.getMethodType();

        if (receiver instanceof CurriedLambdaValue<?, ?, ?> curry) {
            // fixme... seems silly..
            System.err.println(curry.signature() + " " + Arrays.toString(linkRequest.getArguments()));
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

        if (receiver instanceof UncurryValue<?, ?, ?> uncurry) {
            // fixme... seems silly..
            System.err.println(uncurry.signature() + " " + Arrays.toString(linkRequest.getArguments()));

            var mh = TermLinker
                    .link(cs.getLookup(), cs.getOperation(), cs.getMethodType())
                    .dynamicInvoker();

            var parameterCount = methodType.parameterCount();

            mh = linkerServices
                    .asType(mh, methodType.dropParameterTypes(3, parameterCount)
                            .changeReturnType(Term.class));

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

    static final class InvalidationException extends Throwable {
        InvalidationException() {
            super(null, null, false, false);
        }
    }
}

package com.sstewartgallus.runtime;

import com.sstewartgallus.plato.LambdaValue;
import com.sstewartgallus.plato.Term;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.TypeBasedGuardingDynamicLinker;
import jdk.dynalink.linker.support.Guards;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.SwitchPoint;

import static java.lang.invoke.MethodHandles.*;

public final class LambdaLinker implements TypeBasedGuardingDynamicLinker {

    // fixme... can we have a better normalize than just abstract dispatch?
    private static final MethodHandle APPLY_MH;
    private static final InvalidationException INVALIDATION_EXCEPTION = new InvalidationException();

    static {
        try {
            APPLY_MH = lookup().findVirtual(LambdaValue.class, "apply", MethodType.methodType(Term.class, Term.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean canLinkType(Class<?> aClass) {
        return LambdaValue.class.isAssignableFrom(aClass);
    }

    @Override
    public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices) throws Exception {
        var receiver = (LambdaValue<?, ?>) linkRequest.getReceiver();
        var cs = linkRequest.getCallSiteDescriptor();
        var methodType = cs.getMethodType();


        var mh = APPLY_MH;

        // fixme... this should be uncurry's job!
        var parameterCount = methodType.parameterCount();
        if (parameterCount <= 3) {
            mh = dropArguments(mh, 1, Void.class);
            return new GuardedInvocation(
                    linkerServices.asType(mh, methodType),
                    Guards.isOfClass(LambdaValue.class, methodType));
        }
        mh = linkerServices.asType(mh, methodType
                .dropParameterTypes(3, parameterCount)
                .dropParameterTypes(1, 2)
                .changeReturnType(Term.class));

        var oldArgs = linkRequest.getArguments();

        Term<?> result;
        try {
            result = (Term<?>) mh.invoke(oldArgs[0], oldArgs[2]);
        } catch (Exception | Error e) {
            throw e;
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }

        var restTypes = methodType
                .dropParameterTypes(0, 3)
                .insertParameterTypes(0, Term.class, Void.class);

        var arguments = new Object[restTypes.parameterCount()];
        arguments[0] = result;
        arguments[1] = null;
        System.arraycopy(oldArgs, 2, arguments, 2, arguments.length - 2);

        var newRequest = linkRequest.replaceArguments(cs.changeMethodType(restTypes), arguments);
        var guard = linkerServices.getGuardedInvocation(newRequest);
        if (null == guard) {
            return null;
        }

        var fallback = throwException(guard.getInvocation().type().returnType(), InvalidationException.class)
                .bindTo(INVALIDATION_EXCEPTION);
        fallback = dropArguments(fallback, fallback.type().parameterCount(), guard.getInvocation().type().parameterList());

        var handleTheRest = guard.compose(fallback);
        handleTheRest = insertArguments(handleTheRest, 1, (Object) null);
        handleTheRest = dropArguments(handleTheRest, 1, mh.type().parameterList());

        mh = dropArguments(mh, 2, methodType.dropParameterTypes(0, 3).parameterList());

        mh = foldArguments(handleTheRest, 0, mh);

        mh = dropArguments(mh, 1, Void.class);
        return new GuardedInvocation(mh, Guards.isOfClass(LambdaValue.class, methodType), (SwitchPoint) null, InvalidationException.class);
    }

    static final class InvalidationException extends Throwable {
        InvalidationException() {
            super(null, null, false, false);
        }
    }
}


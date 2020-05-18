package com.sstewartgallus.runtime;

import com.sstewartgallus.plato.Interpreter;
import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.ThunkTerm;
import com.sstewartgallus.plato.ValueTerm;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.TypeBasedGuardingDynamicLinker;
import jdk.dynalink.linker.support.Guards;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.SwitchPoint;

import static java.lang.invoke.MethodHandles.*;

public final class ThunkLinker implements TypeBasedGuardingDynamicLinker {

    // fixme... can we have a better normalize than just abstract dispatch?
    private static final MethodHandle NORMALIZE_MH;
    private static final LambdaLinker.InvalidationException INVALIDATION_EXCEPTION = new LambdaLinker.InvalidationException();

    static {
        try {
            NORMALIZE_MH = lookup().findStatic(Interpreter.class, "normalize", MethodType.methodType(ValueTerm.class, Term.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean canLinkType(Class<?> aClass) {
        return ThunkTerm.class.isAssignableFrom(aClass);
    }

    @Override
    public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices) throws Exception {
        var receiver = (ThunkTerm<?>) linkRequest.getReceiver();

        var cs = linkRequest.getCallSiteDescriptor();
        var methodType = cs.getMethodType();
        var oldArgs = linkRequest.getArguments();
        var args = new Object[methodType.parameterCount()];
        args[0] = Interpreter.normalize(receiver);
        System.arraycopy(oldArgs, 1, args, 1, oldArgs.length - 1);

        var newCs = cs.changeMethodType(methodType.changeParameterType(0, ValueTerm.class));
        var guard = linkerServices.getGuardedInvocation(linkRequest.replaceArguments(newCs, args));

        var fallback = throwException(guard.getInvocation().type().returnType(), LambdaLinker.InvalidationException.class)
                .bindTo(INVALIDATION_EXCEPTION);
        fallback = dropArguments(fallback, fallback.type().parameterCount(), guard.getInvocation().type().parameterList());

        var mh = guard.compose(fallback);
        // fixme.. can we specialize even more?

        mh = filterArguments(mh, 0, NORMALIZE_MH);

        return new GuardedInvocation(mh, Guards.isOfClass(ThunkTerm.class, cs.getMethodType()), (SwitchPoint) null, InvalidationException.class);
    }

    static final class InvalidationException extends Throwable {
        InvalidationException() {
            super(null, null, false, false);
        }
    }
}

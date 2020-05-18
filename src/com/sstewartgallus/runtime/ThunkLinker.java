package com.sstewartgallus.runtime;

import com.sstewartgallus.plato.Interpreter;
import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.ThunkTerm;
import com.sstewartgallus.plato.ValueTerm;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.TypeBasedGuardingDynamicLinker;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.methodType;

public final class ThunkLinker implements TypeBasedGuardingDynamicLinker {

    // fixme... can we have a better normalize than just abstract dispatch?
    private static final MethodHandle NORMALIZE_MH;
    private static final InvalidationException INVALIDATION_EXCEPTION = new InvalidationException();

    static {
        try {
            NORMALIZE_MH = lookup().findStatic(Interpreter.class, "normalize", methodType(ValueTerm.class, Term.class));
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

        var guard = linkerServices.getGuardedInvocation(linkRequest.replaceArguments(cs, args));

        return guard.filterArguments(0, NORMALIZE_MH.asType(methodType(methodType.parameterType(0), Term.class)));
    }

    static final class InvalidationException extends Throwable {
        InvalidationException() {
            super(null, null, false, false);
        }
    }
}

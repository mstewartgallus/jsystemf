package com.sstewartgallus.runtime;

import com.sstewartgallus.interpreter.Interpreter;
import com.sstewartgallus.plato.ApplyTerm;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.TypeBasedGuardingDynamicLinker;
import jdk.dynalink.linker.support.Guards;

import java.lang.invoke.MethodHandle;
import java.util.function.Function;

import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.methodType;

public final class ApplyLinker implements TypeBasedGuardingDynamicLinker {
    private static final MethodHandle STEP_THUNK_MH;

    static {
        try {
            STEP_THUNK_MH = lookup().findVirtual(ApplyTerm.class, "step", methodType(Interpreter.class, Interpreter.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean canLinkType(Class<?> aClass) {
        return ApplyTerm.class.isAssignableFrom(aClass);
    }

    @Override
    public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices) throws Exception {
        var receiver = (ApplyTerm<?, ?>) linkRequest.getReceiver();

        var cs = linkRequest.getCallSiteDescriptor();
        var methodType = cs.getMethodType();

        // fixme... specialize for apply

        // fixme.... rename kind of the reverse of a closure, grabs all the arguments then applies them to the result...
        var environment = methodType.dropParameterTypes(0, 2);
        var closure = Closure.spinFactory(environment);
        closure = closure.asType(closure.type().changeReturnType(Function.class));

        var mh = STEP_THUNK_MH;
        mh = dropArguments(mh, mh.type().parameterCount(), environment.parameterList());

        mh = foldArguments(mh, 1, closure);
        mh = dropArguments(mh, 1, Void.class);
        return new GuardedInvocation(mh, Guards.isInstance(ApplyTerm.class, methodType));
    }
}

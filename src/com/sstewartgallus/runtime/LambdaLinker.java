package com.sstewartgallus.runtime;

import com.sstewartgallus.ext.tuples.NilTupleValue;
import com.sstewartgallus.ext.tuples.TuplePairValue;
import com.sstewartgallus.plato.LambdaValue;
import com.sstewartgallus.plato.SimpleLambdaValue;
import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.ValueTerm;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.TypeBasedGuardingDynamicLinker;
import jdk.dynalink.linker.support.Guards;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.SwitchPoint;
import java.util.Arrays;

import static java.lang.invoke.MethodHandles.*;

public final class LambdaLinker implements TypeBasedGuardingDynamicLinker {

    // fixme... can we have a better normalize than just abstract dispatch?
    private static final MethodHandle APPLY_MH;

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

    static Term<?> tuple(Term<?>[] args) {
        ValueTerm current = NilTupleValue.NIL;
        for (var ii = args.length - 1; ii >= 0; --ii) {
            current = new TuplePairValue(args[ii], current);
        }
        return current;
    }
    private static final MethodHandle TUPLE_MH;

    static {
        try {
            TUPLE_MH = lookup().findStatic(LambdaLinker.class, "tuple", MethodType.methodType(Term.class, Term[].class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices) throws Exception {
        var receiver = (LambdaValue<?, ?>) linkRequest.getReceiver();
        var cs = linkRequest.getCallSiteDescriptor();
        var methodType = cs.getMethodType();

        var mh = APPLY_MH;

        var parameterCount = methodType.parameterCount();

        if (parameterCount <= 3) {
            mh = dropArguments(mh, 1, Void.class);
            return new GuardedInvocation(
                    linkerServices.asType(mh, methodType),
                    Guards.isOfClass(LambdaValue.class, methodType));
        }

        mh = dropArguments(mh, 1, Void.class);
        mh = filterArguments(mh, 2, TUPLE_MH);
        mh = mh.asCollector(2, Term[].class, methodType.parameterCount() - 2);

        return new GuardedInvocation(
                linkerServices.asType(mh, methodType),
                Guards.isOfClass(LambdaValue.class, methodType));
    }

}


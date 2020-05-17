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

import static java.lang.invoke.MethodHandles.filterArguments;
import static java.lang.invoke.MethodHandles.lookup;

public final class ThunkLinker implements TypeBasedGuardingDynamicLinker {

    // fixme... can we have a better normalize than just abstract dispatch?
    private static final MethodHandle NORMALIZE_MH;

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
    public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices) {
        var receiver = (ThunkTerm<?>) linkRequest.getReceiver();

        var cs = linkRequest.getCallSiteDescriptor();
        // fixme.. can we specialize even more?
        var mh = TermLinker.link(cs.getLookup(), cs.getOperation(), cs.getMethodType().changeParameterType(0, ValueTerm.class)).dynamicInvoker();

        mh = filterArguments(mh, 0, NORMALIZE_MH);

        return new GuardedInvocation(mh, Guards.isOfClass(ThunkTerm.class, cs.getMethodType()));
    }
}

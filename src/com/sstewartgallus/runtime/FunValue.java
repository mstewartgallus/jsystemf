package com.sstewartgallus.runtime;

import jdk.dynalink.*;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.support.Guards;

// fixme... break out into another class for env capturing closures...
public abstract class FunValue<T> extends Value<T> {
    protected abstract int arity();

    protected abstract GuardedInvocation saturatedApplication(LinkRequest linkRequest, LinkerServices linkerServices) throws NoSuchFieldException, IllegalAccessException;

    // fixme... look furether into   https://gitlab.haskell.org/ghc/ghc/-/wikis/commentary/rts/haskell-execution/function-calls
    final GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices) throws NoSuchFieldException, IllegalAccessException {
        Operation operation = linkRequest.getCallSiteDescriptor().getOperation();
        Object name = null;
        boolean wasNamed = false;
        if (operation instanceof NamedOperation named) {
            name = named.getName();
            wasNamed = true;
            operation = named.getBaseOperation();
        }
        Namespace[] namespaces = null;
        if (operation instanceof NamespaceOperation namespaced) {
            namespaces = namespaced.getNamespaces();
            operation = namespaced.getBaseOperation();
        }

        if (operation instanceof StandardOperation standard) {
            if (standard == StandardOperation.CALL) {
                return getStandardCall(linkRequest, linkerServices);
            }
        }
        return null;
    }

    private GuardedInvocation getStandardCall(LinkRequest linkRequest, LinkerServices linkerServices) throws NoSuchFieldException, IllegalAccessException {
        var cs = linkRequest.getCallSiteDescriptor();
        var calledWithParams = cs.getMethodType().parameterCount() - 2;

        var arity = arity();

        if (calledWithParams < arity) {
            return partialApplication(calledWithParams, linkRequest, linkerServices);
        }

        if (calledWithParams > arity) {
            System.err.println("arity " + arity + " " + linkRequest.getCallSiteDescriptor().getMethodType());
            return superSaturatedApplication(linkRequest, linkerServices);
        }

        return saturatedApplication(linkRequest, linkerServices);
    }

    private GuardedInvocation superSaturatedApplication(LinkRequest linkRequest, LinkerServices linkerServices) {
        throw new UnsupportedOperationException("unimplemented");
    }

    private GuardedInvocation partialApplication(int calledWith, LinkRequest linkRequest, LinkerServices linkerServices) {
        // fixme... drop reciever/null receiver arguments....
        var methodType = linkRequest.getCallSiteDescriptor().getMethodType();
        methodType = methodType.dropParameterTypes(0, 2);

        var mh = Closure.spinFactory(methodType);

        // fixme.. guard on arities...
        return new GuardedInvocation(mh, Guards.isOfClass(getClass(), mh.type().changeReturnType(boolean.class)));
    }
}


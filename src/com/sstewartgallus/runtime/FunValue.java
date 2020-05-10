package com.sstewartgallus.runtime;

import jdk.dynalink.*;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.support.Guards;

import java.lang.invoke.MethodHandle;
import java.util.stream.Collectors;

import static java.lang.invoke.MethodHandles.*;

// fixme... break out into another class for environment capturing closures...
public abstract class FunValue<T> extends Value<T> {
    public abstract Infotable infoTable();

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
        var metadata = infoTable();

        var cs = linkRequest.getCallSiteDescriptor();
        var calledWithParams = cs.getMethodType().parameterCount() - 2;

        var arity = metadata.arguments().size();

        if (calledWithParams < arity) {
            return partialApplication(metadata, calledWithParams, linkRequest, linkerServices);
        }

        if (calledWithParams > arity) {
            return superSaturatedApplication(metadata, linkRequest, linkerServices);
        }

        return saturatedApplication(metadata, linkRequest, linkerServices);
    }

    private GuardedInvocation superSaturatedApplication(Infotable metadata, LinkRequest linkRequest, LinkerServices linkerServices) {
        throw new UnsupportedOperationException("unimplemented");
    }

    private GuardedInvocation partialApplication(Infotable metadata, int calledWith, LinkRequest linkRequest, LinkerServices linkerServices) {
        // fixme... make this work for closures as well... ?
        var arguments = metadata.arguments();
        var entryPoint = metadata.entryPoint();

        var calledArgs = arguments.stream().limit(calledWith).collect(Collectors.toUnmodifiableList());
        var freeArgs = arguments.stream().skip(calledWith + 1).collect(Collectors.toUnmodifiableList());

        var mh = Closure.spinFactory(calledArgs, freeArgs, entryPoint);

        mh = dropArguments(mh, 0, Value.class, Void.class);

        return new GuardedInvocation(mh, Guards.isOfClass(getClass(), mh.type().changeReturnType(boolean.class)));
    }

    private GuardedInvocation saturatedApplication(Infotable metadata, LinkRequest linkRequest, LinkerServices linkerServices) throws NoSuchFieldException, IllegalAccessException {
        var argument = metadata.arguments();
        var environment = metadata.environment();
        var execute = metadata.entryPoint();

        MethodHandle mh;
        if (environment.isEmpty()) {
            mh = execute;
            mh = dropArguments(mh, 0, Value.class, Void.class);
        } else {
            // fixme... handle environment better...
            var environmentGetter = lookup().findGetter(getClass(), "environment", environment.get(0));
            mh = filterArguments(execute, 0, environmentGetter);


            // fit into the stupid dummy receiver thing...
            mh = dropArguments(mh, 1, Void.class);
            // fixme... also guard on thunk being not fully saturated...
            mh = mh.asType(mh.type().changeParameterType(0, Value.class));
        }

        return new GuardedInvocation(mh, Guards.isOfClass(getClass(), mh.type().changeReturnType(boolean.class)));
    }
}


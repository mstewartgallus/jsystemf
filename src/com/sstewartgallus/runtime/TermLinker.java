package com.sstewartgallus.runtime;

import com.sstewartgallus.ext.java.IntValue;
import com.sstewartgallus.ext.mh.JitLinker;
import com.sstewartgallus.interpreter.Interpreter;
import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.ValueTerm;
import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.DynamicLinker;
import jdk.dynalink.DynamicLinkerFactory;
import jdk.dynalink.Operation;
import jdk.dynalink.linker.*;
import jdk.dynalink.support.SimpleRelinkableCallSite;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.function.Supplier;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

public final class TermLinker implements TypeBasedGuardingDynamicLinker, GuardingTypeConverterFactory {
    private static final DynamicLinker DYNAMIC_LINKER;
    private static final MethodHandle NORMALIZE_MH;

    // fixme.. look more into https://gitlab.haskell.org/ghc/ghc/-/wikis/commentary/rts/haskell-execution/function-calls
    private static final MethodHandle INT_VALUE_CONSTRUCTOR_MH;
    private static final MethodHandle INT_VALUE_MH;

    static {
        var linkers = List.of(new JitLinker(), new TypeLambdaLinker(), new LambdaLinker(), new TermLinker());
        var factory = new DynamicLinkerFactory();
        factory.setPrioritizedLinkers(linkers);
        factory.setSyncOnRelink(true);
        DYNAMIC_LINKER = factory.createLinker();
    }

    static {
        try {
            NORMALIZE_MH = lookup().findStatic(TermLinker.class, "normalize", methodType(ValueTerm.class, Term.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        try {
            INT_VALUE_CONSTRUCTOR_MH = lookup().findConstructor(IntValue.class, methodType(void.class, int.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        try {
            INT_VALUE_MH = lookup().findVirtual(IntValue.class, "value", methodType(int.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // fixme... no need for so many uses...
    public static CallSite link(MethodHandles.Lookup lookup, Operation operation, MethodType methodType) {
        return DYNAMIC_LINKER.link(
                new SimpleRelinkableCallSite(
                        new CallSiteDescriptor(lookup, operation, methodType)));
    }

    private static <A> ValueTerm<A> normalize(Term<A> term) {
        // fixme... get rid of casting if possible...
        return (ValueTerm<A>) Interpreter.interpret(term.interpret());
    }

    @Override
    public boolean canLinkType(Class<?> aClass) {
        return ValueTerm.class.isAssignableFrom(aClass);
    }

    @Override
    public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices) throws NoSuchFieldException, IllegalAccessException {
        // fixme... do stuff for other values like MethodHandleThunk?
        var receiver = (Term<?>) linkRequest.getReceiver();

        System.err.println("linking term " + receiver.getClass() + " " + receiver);

        return null;
    }

    // fixme... move around....
    @Override
    public GuardedInvocation convertToType(Class<?> sourceType, Class<?> targetType, Supplier<MethodHandles.Lookup> lookupSupplier) throws Exception {
        if (sourceType == int.class && Term.class.isAssignableFrom(targetType)) {
            return new GuardedInvocation(INT_VALUE_CONSTRUCTOR_MH.asType(methodType(targetType, int.class)));
        }

        if (IntValue.class.isAssignableFrom(sourceType) && Number.class == targetType) {
            // fixme... check int type dynamically by calling type()
            return new GuardedInvocation(INT_VALUE_MH.asType(methodType(Number.class, sourceType)));
        }

        if (ValueTerm.class.isAssignableFrom(sourceType)) {
            if (int.class == targetType) {
                // fixme... check int type dynamically by calling type()
                return new GuardedInvocation(INT_VALUE_MH.asType(methodType(int.class, sourceType)));
            }
            return null;
        }

        if (Term.class.isAssignableFrom(sourceType)) {
            if (ValueTerm.class.isAssignableFrom(targetType)) {
                return new GuardedInvocation(NORMALIZE_MH.asType(methodType(targetType, Term.class)));
            }

            unboxToPrim:
            {
                var guard = convertToType(ValueTerm.class, targetType, lookupSupplier);
                if (guard == null) {
                    break unboxToPrim;
                }

                return guard.filterArguments(0, NORMALIZE_MH);
            }
        }
        return null;
    }
}

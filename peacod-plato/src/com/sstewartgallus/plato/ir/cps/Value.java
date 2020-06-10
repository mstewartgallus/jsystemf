package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.type.TypeDesc;

import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;
import java.util.Set;

public interface Value<A> {
    default A interpret(CpsEnvironment environment) {
        throw new UnsupportedOperationException(getClass().toString());
    }

    default void compile(CompilerEnvironment environment) {
        throw new UnsupportedOperationException(getClass().toString());
    }

    default A compile(MethodHandles.Lookup lookup, PrintWriter writer) {
        // Unless a value is a closure we assume we can just compute it instead of compiling..
        return interpret(new CpsEnvironment(lookup));
    }

    Set<LocalValue<?>> dependencies();

    TypeDesc<A> type();
}

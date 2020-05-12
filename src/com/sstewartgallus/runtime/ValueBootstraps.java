package com.sstewartgallus.runtime;

import com.sstewartgallus.plato.Type;

import java.lang.invoke.MethodHandles;

public final class ValueBootstraps {
    private ValueBootstraps() {
    }

    @SuppressWarnings("unused")
    public static <T, V extends Value<T>> V of(MethodHandles.Lookup lookup, String name, Class<V> klass, Class<?> declaringClass, Type<T> type) {
        throw new UnsupportedOperationException("unimplemented");
    }
/*
    @SuppressWarnings("unused")
    <T, V extends Value<T>> V term​(MethodHandles.Lookup lookup, String name, Class<V> klass, Term<T> term, Type<T> type) {
        throw null;
    } */
}
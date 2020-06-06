package com.sstewartgallus.plato.syntax.type;

import com.sstewartgallus.plato.runtime.Fun;
import com.sstewartgallus.plato.runtime.V;

import java.lang.constant.ConstantDesc;
import java.util.Optional;

public final class FunctionTag<A, B> implements TypeTag<V<A, V<B, Fun<A, B>>>> {
    private static final FunctionTag FUNCTION_TAG = new FunctionTag();

    public static <A, B> FunctionTag<A, B> function() {
        return FUNCTION_TAG;
    }

    @Override
    public String toString() {
        return "(->)";
    }

    @Override
    public Optional<? extends ConstantDesc> describeConstable() {
        return Optional.of(TypeDesc.ofFunction());
    }

    @Override
    public Class<?> erase() {
        return Object.class;
    }
}
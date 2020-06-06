package com.sstewartgallus.plato.runtime.internal;

import java.util.function.Supplier;

public final class SupplierClassValue<T> extends ClassValue<T> {
    private final Supplier<T> supplier;

    public SupplierClassValue(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    protected T computeValue(Class<?> type) {
        return supplier.get();
    }
}

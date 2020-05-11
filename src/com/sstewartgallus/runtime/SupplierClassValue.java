package com.sstewartgallus.runtime;

import java.util.function.Supplier;

final class SupplierClassValue<T> extends ClassValue<T> {
    private final Supplier<T> supplier;

    SupplierClassValue(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    protected T computeValue(Class<?> type) {
        return supplier.get();
    }
}

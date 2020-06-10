package com.sstewartgallus.plato.ir.type;

import java.util.Optional;

abstract class NamedType<A> implements Type<A> {
    private final String packageName;
    private final String className;

    NamedType(String packageName, String className) {
        this.packageName = packageName;
        this.className = className;
    }

    @Override
    public final String toString() {
        return packageName + "/" + className;
    }

    @Override
    public final Optional<TypeDesc<A>> describeConstable() {
        return Optional.of(TypeDesc.ofReference(packageName, className));
    }
}
package com.sstewartgallus.plato.ir.type;

final class IntType extends NamedType<Integer> implements RealType<Integer> {
    public static final IntType INT_TYPE = new IntType();

    private IntType() {
        super("core", "#int");
    }

    @Override
    public Class<?> erase() {
        return int.class;
    }

    @Override
    public Integer cast(Object value) {
        return (Integer) value;
    }
}
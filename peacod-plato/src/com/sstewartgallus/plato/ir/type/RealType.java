package com.sstewartgallus.plato.ir.type;

// fixme... come up with a better name..., see if I can hide...
public interface RealType<A> extends Type<A> {
    Class<?> erase();

    A cast(Object value);
}
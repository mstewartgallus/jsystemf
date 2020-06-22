package com.sstewartgallus.plato.ir.cps;

public interface ValueVisitor {
    <C> Value<C> onValue(Value<C> value);
}

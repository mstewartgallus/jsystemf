package com.sstewartgallus.plato.ir.cps;

public interface ActionVisitor {
    <C> Action<C> onAction(Action<C> action);
}

package com.sstewartgallus.plato.ir.cbpv;

public interface CodeVisitor {
    <C> Code<C> onCode(Code<C> code);
}

package com.sstewartgallus.plato.ir.dethunk;

public interface CodeVisitor {
    <C> Does<C> onCode(Does<C> code);
}

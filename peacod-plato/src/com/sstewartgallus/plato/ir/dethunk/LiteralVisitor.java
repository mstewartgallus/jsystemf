package com.sstewartgallus.plato.ir.dethunk;

public interface LiteralVisitor {
    <C> Thing<C> onLiteral(Thing<C> literal);
}

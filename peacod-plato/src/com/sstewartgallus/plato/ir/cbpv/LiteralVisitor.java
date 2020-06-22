package com.sstewartgallus.plato.ir.cbpv;

public interface LiteralVisitor {
    <C> Literal<C> onLiteral(Literal<C> literal);
}

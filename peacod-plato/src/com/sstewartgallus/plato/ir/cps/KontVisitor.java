package com.sstewartgallus.plato.ir.cps;

public interface KontVisitor {
    <C> Kont<C> onKont(Kont<C> kont);
}

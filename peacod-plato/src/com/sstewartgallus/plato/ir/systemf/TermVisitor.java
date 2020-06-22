package com.sstewartgallus.plato.ir.systemf;

public interface TermVisitor {
    <C> Term<C> onTerm(Term<C> term);
}

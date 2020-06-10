package com.sstewartgallus.plato.ir.systemf;

import com.sstewartgallus.plato.ir.type.TypeDesc;

/**
 * The high level syntax for the core System F terms in my little language.
 * <p>
 * This is intended to be pristine source language untainted by compiler stuff.
 * <p>
 * Any processing should happen AFTER this step.
 * <p>
 * See https://gitlab.haskell.org/ghc/ghc/-/wikis/commentary/compiler/core-syn-type
 * and https://github.com/DanBurton/Blog/blob/master/Literate%20Haskell/SystemF.lhs
 * for inspiration.
 * See http://cs.ioc.ee/efftt/levy-slides.pdf
 */
public interface Term<A> {
    TypeDesc<A> type();

}
package com.sstewartgallus.plato.runtime;

import com.sstewartgallus.plato.ir.cps.Lbl;

public record CccStack<C, A, B>(Lbl<A>label, Stack<C, B>next) implements Stack<C, A> {
}
